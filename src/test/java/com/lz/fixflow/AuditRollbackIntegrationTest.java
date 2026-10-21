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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * 集成测试：真实 Spring 事务 + 真实 MySQL 审计写入。
 * 只 Mock TicketHistoryMapper.insert，故意让业务在 UPDATE 后失败。
 * 切勿连接业务数据库；仅在独立的 fixflow_test 库中执行。
 */
@SpringBootTest
@ActiveProfiles("test")
class AuditRollbackIntegrationTest {

    @Autowired private TicketService ticketService; // 真实 Spring 代理
    @Autowired private JdbcTemplate jdbc;            // 真实 MySQL

    @MockitoBean(enforceOverride = true)
    private TicketHistoryMapper ticketHistoryMapper; // 仅模拟历史写入故障

    private String ticketId;
    private String staffId;
    private Long categoryId;
    private StpLogic originalLogic;
    private boolean databaseVerified;

    @BeforeEach
    void setUp() {
        // 第一件事：确认不是平时的 fixflow 数据库，防止误操作。
        assertEquals("fixflow_test",
                jdbc.queryForObject("SELECT DATABASE()", String.class));
        databaseVerified = true;

        // 准备独立的分类、待处理工单。
        String categoryName = "audit-rollback" + UUID.randomUUID().toString().substring(0,8);
        jdbc.update("INSERT INTO category (name) VALUES (?)", categoryName);
        categoryId = jdbc.queryForObject(
                "SELECT id FROM category WHERE name = ?", Long.class, categoryName);

        ticketId = UUID.randomUUID().toString();
        staffId = UUID.randomUUID().toString();
        jdbc.update("""
                INSERT INTO ticket
                    (id, title, description, priority, status, creator_id, category_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                ticketId,
                "审计事务集成测试",
                "验证业务回滚后失败审计仍保留",
                "MEDIUM",
                TicketStatus.OPEN.getValue(),
                UUID.randomUUID().toString(),
                categoryId
            );

        // 无浏览器登录：测试期间让 Sa-Token 返回固定的测试 STAFF ID。
        originalLogic = StpUtil.getStpLogic();
        StpUtil.setStpLogic(new StpLogic(originalLogic.getLoginType()) {
            @Override
            public boolean isLogin() { return true; }

            @Override
            public String getLoginIdAsString() { return staffId; }
        });

        // @Audit 切面需要 HTTP 请求上下文；测试线程手动放入模拟请求。
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request));
    }

    @Test
    @DisplayName("工单回滚时，失败审计通过独立事务写入数据库")
    void rollbackBusiness_butKeepFailureAudit() {
        // Arrange：只有工单历史 Mapper 是假的，接单 UPDATE 和审计 INSERT 都是真的。
        doThrow(new IllegalStateException("模拟历史记录写入失败"))
                .when(ticketHistoryMapper)
                .insert(any(TicketHistory.class));

        // Act：调用真实的 Spring Service，触发事务和 @Audit 切面。
        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> ticketService.handleTicket(ticketId));
        assertEquals("模拟历史记录写入失败", failure.getMessage());

        // 确认异常确实发生在写入历史的步骤，不是提前失败。
        verify(ticketHistoryMapper).insert(any(TicketHistory.class));

        // Assert 1：事务回滚，工单保持 OPEN、无人接单。
        assertEquals(TicketStatus.OPEN.getValue(), jdbc.queryForObject(
                "SELECT status FROM ticket WHERE id = ?", String.class, ticketId));
        assertNull(jdbc.queryForObject(
                "SELECT assignee_id FROM ticket WHERE id = ?", String.class, ticketId));

        // Assert 2：审计记录仍然存在，明确标为失败。
        Long auditCount = jdbc.queryForObject("""
                SELECT COUNT(*) FROM audit_log
                WHERE user_id = ? AND action = ? AND success = 0
                """, Long.class, staffId, "工单已受理");
        assertEquals(1L, auditCount);

        String errorMessage = jdbc.queryForObject("""
                SELECT error_message FROM audit_log
                WHERE user_id = ? AND action = ? AND success = 0
                """, String.class, staffId, "工单已受理");
        assertEquals("模拟历史记录写入失败", errorMessage);
    }

    @AfterEach
    void tearDown() {
        try {
            if (databaseVerified) {
                if (ticketId != null) {
                    jdbc.update("DELETE FROM ticket_history WHERE ticket_id = ?", ticketId);
                    jdbc.update("DELETE FROM ticket WHERE id = ?", ticketId);
                }
                if (staffId != null) {
                    jdbc.update("DELETE FROM audit_log WHERE user_id = ?", staffId);
                }
                if (categoryId != null) {
                    jdbc.update("DELETE FROM category WHERE id = ?", categoryId);
                }
            }
        } finally {
            RequestContextHolder.resetRequestAttributes();
            if (originalLogic != null)
                StpUtil.setStpLogic(originalLogic);
        }
    }
}
