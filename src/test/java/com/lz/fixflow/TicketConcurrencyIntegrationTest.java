package com.lz.fixflow;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lz.fixflow.entity.Ticket;
import com.lz.fixflow.enums.TicketStatus;
import com.lz.fixflow.mapper.TicketMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MySQL 集成测试：验证两个线程竞争同一张待接单工单时，只有一个 UPDATE 成功。
 * 注意：这个测试直接调用真实 Mapper，不涉及 Sa-Token、业务历史和 @Audit。
 * 只在独立的 fixflow_test 数据库运行，不能用正式业务库。
 */
@SpringBootTest
@ActiveProfiles("test")
class TicketConcurrencyIntegrationTest {

    @Autowired
    private TicketMapper ticketMapper; // 真实 Mapper，不是 @Mock

    @Autowired
    private JdbcTemplate jdbc; // 向测试数据库准备和清理数据

    private String ticketId;
    private Long categoryId;

    @BeforeEach
    void prepareTicket() {
        // 每次测试使用随机编号，避免和其他测试的数据冲突。
        String categoryName = "it-" + UUID.randomUUID().toString().substring(0, 8);
        jdbc.update("INSERT INTO category (name) VALUES (?)", categoryName);
        categoryId = jdbc.queryForObject(
                "SELECT id FROM category WHERE name = ?",
                Long.class,
                categoryName
        );

        ticketId = UUID.randomUUID().toString();
        jdbc.update("""
                INSERT INTO ticket
                    (id, title, description, priority, status, creator_id, category_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                ticketId,
                "并发接单测试工单",
                "两位工作人员同时尝试接单",
                "MEDIUM",
                TicketStatus.OPEN.getValue(),
                UUID.randomUUID().toString(),
                categoryId
        );
    }

    @AfterEach
    void cleanTestData() {
        // 删除当前测试创建的数据，不删除其他工单。
        if (ticketId != null) {
            jdbc.update("DELETE FROM ticket WHERE id = ?", ticketId);
        }
        if (categoryId != null) {
            jdbc.update("DELETE FROM category WHERE id = ?", categoryId);
        }
    }

    @Test
    void twoWorkersOnlyOneCanClaim() throws Exception {
        String staffA = UUID.randomUUID().toString();
        String staffB = UUID.randomUUID().toString();

        // 两个线程相当于两名工作人员，ready/start 用来尽量同时发起更新。
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicBoolean allowclaim = new AtomicBoolean(false);

        Callable<Integer> taskA = () -> {
            ready.countDown();

            start.await();
            if (!allowclaim.get()) {
                throw new IllegalStateException("测试未正常发令");
            }
            return claimInDatabase(staffA);
        };
        Callable<Integer> taskB = () -> {
            ready.countDown();

            start.await();
            if (!allowclaim.get()) {
                throw new IllegalStateException("测试未正常发令");
            }
            return claimInDatabase(staffB);
        };

        try {
            Future<Integer> futureA = pool.submit(taskA);
            Future<Integer> futureB = pool.submit(taskB);

            assertTrue(ready.await(10, TimeUnit.SECONDS), "两个工作人员都应先准备就绪");

            allowclaim.set(true);
            start.countDown(); // 发令：让两个线程开始竞争同一工单

            int rowsA = futureA.get(15, TimeUnit.SECONDS);
            int rowsB = futureB.get(15, TimeUnit.SECONDS);

            System.out.println("STAFF A 更新行数 = " + rowsA);
            System.out.println("STAFF B 更新行数 = " + rowsB);

            // 预期只有一个成功（1 行），另一个因 WHERE 条件不满足而失败（0 行）。
            assertTrue(
                    (rowsA == 1 && rowsB == 0) || (rowsA == 0 && rowsB == 1),
                    "必须恰好一个人接单成功"
            );

            // 再从数据库读一遍，确认最终状态与获胜者一致。
            Ticket actual = ticketMapper.selectById(ticketId);
            assertEquals(TicketStatus.IN_PROGRESS.getValue(), actual.getStatus());
            assertEquals(rowsA == 1 ? staffA : staffB, actual.getAssigneeId());
        } finally {
            start.countDown(); // 若前面断言失败，也释放可能在等待的线程
            pool.shutdownNow();
        }
    }

    private int claimInDatabase(String staffId) {
        // 与 TicketServiceImpl.handleTicket() 中的关键条件更新保持一致。
        // 这里直接执行真实 Mapper，因此不需要模拟登录，也不会写入工单历史。
        return ticketMapper.update(null,
                new LambdaUpdateWrapper<Ticket>()
                        .eq(Ticket::getStatus, TicketStatus.OPEN.getValue())
                        .eq(Ticket::getId, ticketId)
                        .isNull(Ticket::getAssigneeId)
                        .set(Ticket::getStatus, TicketStatus.IN_PROGRESS.getValue())
                        .set(Ticket::getAssigneeId, staffId)
        );
    }
}
