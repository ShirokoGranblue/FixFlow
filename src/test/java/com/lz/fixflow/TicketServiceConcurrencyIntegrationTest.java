package com.lz.fixflow;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import com.lz.fixflow.entity.Ticket;
import com.lz.fixflow.enums.TicketAction;
import com.lz.fixflow.enums.TicketStatus;
import com.lz.fixflow.mapper.TicketMapper;
import com.lz.fixflow.service.TicketService;
import com.lz.fixflow.common.BusinessException;
import com.lz.fixflow.vo.TicketVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 在独立的 fixflow_test MySQL 数据库中测试真正的 Spring Service、事务和工单历史。
 * 没有 @Transactional 修饰测试方法：并发线程应使用自己的真实事务。
 * 本测试通过 ThreadLocal 模拟不同登录人；不测试 HTTP/角色鉴权。
 * 注意：测试期间会临时替换 Sa-Token 全局 StpLogic，不要与其他测试并行执行。
 */
@SpringBootTest
@ActiveProfiles("test")
class TicketServiceConcurrencyIntegrationTest {

    @Autowired private TicketService ticketService; // Spring Bean：包括 @Transactional / @Audit 代理
    @Autowired private TicketMapper ticketMapper;   // 真实 MyBatis Mapper
    @Autowired private JdbcTemplate jdbc;

    private final ThreadLocal<String> currentStaff = new ThreadLocal<>();
    private AtomicBoolean allowStart = new AtomicBoolean(false);
    private StpLogic originalLogic;

    private String ticketId;
    private Long categoryId;

    private String staffA;
    private String staffB;

    @BeforeEach
    void prepare() {
        // 1. 为每个测试建立独立数据，避免和其他测试互相干扰。
        String categoryName = "it-" + UUID.randomUUID();
        jdbc.update("INSERT INTO category (name) VALUES (?)", categoryName);
        categoryId = jdbc.queryForObject(
                "SELECT id FROM category WHERE name = ?", Long.class, categoryName);

        ticketId = UUID.randomUUID().toString();
        staffA = UUID.randomUUID().toString();
        staffB = UUID.randomUUID().toString();
        jdbc.update("""
                INSERT INTO ticket (id, title, description, priority, status, creator_id, category_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                ticketId, "并发接单集成测试", "两个STAFF同时尝试接单", "MEDIUM",
                TicketStatus.OPEN.getValue(), UUID.randomUUID().toString(), categoryId);

        // 2. Service 通过 StpUtil.getLoginIdAsString() 获取当前人。
        // 不同线程从 ThreadLocal 取自己的 ID，不能使用单个全局固定 ID。
        originalLogic = StpUtil.getStpLogic();
        StpUtil.setStpLogic(new StpLogic(originalLogic.getLoginType()) {
            @Override
            public String getLoginIdAsString() {
                return currentStaff.get();
            }
            @Override
            public boolean isLogin() {
                return currentStaff.get() != null;
            }
        });
    }

    @AfterEach
    void clean() {
        try {
            if (ticketId != null) {
                jdbc.update("DELETE FROM ticket_history WHERE ticket_id = ?", ticketId);
                jdbc.update("DELETE FROM ticket WHERE id = ?", ticketId);
            }
            if (categoryId != null) {
                jdbc.update("DELETE FROM category WHERE id = ?", categoryId);
            }
            if (staffA != null && staffB != null) {
                jdbc.update("DELETE FROM audit_log WHERE user_id IN (?, ?)", staffA, staffB);
            }
        } finally {
            if (originalLogic != null)
                StpUtil.setStpLogic(originalLogic);

            currentStaff.remove();
        }
    }

    private Callable<Boolean> staffJob(
            String staffId,
            CountDownLatch ready,
            CountDownLatch start) {

        return () -> {
            currentStaff.set(staffId); // 当前线程的登录人

            // 当前项目的 @Audit 从 RequestContextHolder 取 HttpServletRequest；
            // 测试没有浏览器请求，给每个工作线程设置自己的模拟请求。
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRemoteAddr("127.0.0.1");
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

            try {
                ready.countDown(); // 报告“我准备好了”
                start.await();
                if (!allowStart.get()) {
                    throw new IllegalStateException("等待统一开始接单超时");
                }

                try {
                    // 真正调用 Spring 管理的 Service，不是直接调用 Mapper
                    TicketVO result = ticketService.handleTicket(ticketId);
                    assertEquals(staffId, result.getAssigneeId());
                    return true; // 接单成功
                } catch (BusinessException ex) {
                    assertEquals("工单不存在或已被处理", ex.getMessage());
                    return false; // 条件 UPDATE 影响 0 行，接单失败
                }
            } finally {
                currentStaff.remove();
                RequestContextHolder.resetRequestAttributes();
            }
        };
    }

    @Test
    void onlyOneStaffSucceedsAndOneHistoryIsWritten() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Future<Boolean> futureA = pool.submit(staffJob(staffA, ready, start));
            Future<Boolean> futureB = pool.submit(staffJob(staffB, ready, start));

            assertTrue(ready.await(10, TimeUnit.SECONDS), "两名STAFF必须先准备就绪");
            allowStart.set(true);
            start.countDown(); // 发令枪：放行两个线程

            boolean aSuccess = futureA.get(20, TimeUnit.SECONDS);
            boolean bSuccess = futureB.get(20, TimeUnit.SECONDS);

            System.out.printf("STAFF A成功=%s，STAFF B成功=%s%n", aSuccess, bSuccess);
            assertEquals(1, (aSuccess ? 1 : 0) + (bSuccess ? 1 : 0), "必须恰好一人接单成功");

            // 检查真实 MySQL 数据
            Ticket actual = ticketMapper.selectById(ticketId);
            assertNotNull(actual);
            assertEquals(TicketStatus.IN_PROGRESS.getValue(), actual.getStatus());
            assertEquals(aSuccess ? staffA : staffB, actual.getAssigneeId());

            Long historyCount = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM ticket_history WHERE ticket_id = ? AND action = ?",
                    Long.class, ticketId, TicketAction.HANDLE.getValue());
            assertEquals(Long.valueOf(1L), historyCount, "成功接单只应产生一条 HANDLE 历史");

            String historyOperator = jdbc.queryForObject(
                    "SELECT operator_id FROM ticket_history WHERE ticket_id = ? AND action = ?",
                    String.class, ticketId, TicketAction.HANDLE.getValue());
            assertEquals(actual.getAssigneeId(), historyOperator, "历史记录人必须是成功接单的人");
        } finally {
            start.countDown(); // 发生异常时避免有线程一直等待
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS), "测试线程应及时结束");
        }
    }
}
