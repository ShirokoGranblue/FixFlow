package com.lz.fixflow;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import com.lz.fixflow.entity.TicketHistory;
import com.lz.fixflow.enums.TicketStatus;
import com.lz.fixflow.mapper.TicketHistoryMapper;
import com.lz.fixflow.service.TicketService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * 测试真正的 Spring 事务：TicketMapper 操作真实 MySQL；
 * 仅将 TicketHistoryMapper 替换成 Mockito Mock，用于在历史写入时故意抛异常。
 * 请使用独立的 fixflow_test 数据库；不要在真实业务数据库执行。
 */
@SpringBootTest
@ActiveProfiles("test")
class TicketTransactionRollbackIntegrationTest {

    @Autowired
    private TicketService ticketService; // Spring 管理的真实 Service（含事务代理）

    @Autowired
    private JdbcTemplate jdbc; // 实际连接测试 MySQL

    @MockitoBean(enforceOverride = true)
    private TicketHistoryMapper ticketHistoryMapper; // 只模拟历史写入

    private StpLogic originalLogic;
    private String ticketId;
    private String staffId;
    private Long categoryId;

    @BeforeEach
    void prepare() {
        // 1. 创建只供本次测试使用的分类和待处理工单。
        String categoryName = "rollback-it-" + UUID.randomUUID();
        jdbc.update("INSERT INTO category (name) VALUES (?)", categoryName);
        categoryId = jdbc.queryForObject(
                "SELECT id FROM category WHERE name = ?", Long.class, categoryName);

        ticketId = UUID.randomUUID().toString();
        staffId = UUID.randomUUID().toString();
        jdbc.update("""
                INSERT INTO ticket (id, title, description, priority, status, creator_id, category_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                ticketId, "接单回滚集成测试", "模拟历史写入失败", "MEDIUM",
                TicketStatus.OPEN.getValue(), UUID.randomUUID().toString(), categoryId);

        // 2. 本测试没有真实 HTTP 登录，模拟 Service 取得的当前工作人员 ID。
        originalLogic = StpUtil.getStpLogic();
        StpUtil.setStpLogic(new StpLogic(originalLogic.getLoginType()) {
            @Override
            public String getLoginIdAsString() {
                return staffId;
            }

            @Override
            public boolean isLogin() {
                return true;
            }
        });

        // 3. 项目 @Audit 切面读取 HttpServletRequest；这里准备一个模拟请求。
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    @DisplayName("历史记录写入失败时，接单状态和处理人必须回滚")
    void historyInsertFailed_shouldRollbackClaim() {
        // 防止误用未经 Spring AOP 代理的 Service 对象。
        assertTrue(AopUtils.isAopProxy(ticketService), "Service 应由 Spring AOP 代理");

        // Arrange：历史 Mapper 插入时故意抛错；工单 Mapper 仍然是真实 MySQL。
        doThrow(new IllegalStateException("模拟历史写入失败"))
                .when(ticketHistoryMapper)
                .insert(any(TicketHistory.class));

        // Act + Assert：调用真正的 handleTicket()，预期异常向外传播。
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ticketService.handleTicket(ticketId)
        );
        assertEquals("模拟历史写入失败", exception.getMessage());
        verify(ticketHistoryMapper).insert(any(TicketHistory.class));

        // Assert：业务方法已经退出并触发事务回滚，再查真实 MySQL。
        String actualStatus = jdbc.queryForObject(
                "SELECT status FROM ticket WHERE id = ?", String.class, ticketId);
        String actualAssignee = jdbc.queryForObject(
                "SELECT assignee_id FROM ticket WHERE id = ?", String.class, ticketId);
        Long actualHistoryCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ticket_history WHERE ticket_id = ?", Long.class, ticketId);

        assertEquals(TicketStatus.OPEN.getValue(), actualStatus,
                "接单 UPDATE 必须回滚为 OPEN");
        assertNull(actualAssignee, "处理人必须回滚为 NULL");
        assertEquals(0L, actualHistoryCount.longValue(), "不应留下接单历史");
    }

    @AfterEach
    void cleanup() {
        try {
            // 使用 JdbcTemplate 清理，不经过被 Mockito 替换的 Mapper。
            if (ticketId != null) {
                jdbc.update("DELETE FROM ticket_history WHERE ticket_id = ?", ticketId);
                jdbc.update("DELETE FROM ticket WHERE id = ?", ticketId);
            }
            if (categoryId != null) {
                jdbc.update("DELETE FROM category WHERE id = ?", categoryId);
            }
            if (staffId != null) {
                jdbc.update("DELETE FROM audit_log WHERE user_id = ?", staffId);
            }
        } finally {
            RequestContextHolder.resetRequestAttributes();
            if (originalLogic != null) {
                StpUtil.setStpLogic(originalLogic);
            }
        }
    }
}
