package com.lz.fixflow;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import com.lz.fixflow.annotation.Audit;
import com.lz.fixflow.aspect.AuditAspect;
import com.lz.fixflow.common.BusinessException;
import com.lz.fixflow.entity.AuditLog;
import com.lz.fixflow.mapper.AuditLogMapper;
import com.lz.fixflow.service.AuditLogService;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

/**
 * AuditAspect 的单元测试：真实执行切面和日志服务，模拟被审计业务和审计 Mapper。
 * 不启动 Spring，不连接 MySQL；不能据此证明 AOP 代理生效或事务日志能持久化。
 */
@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

    private static final String STAFF_ID = "f1185aa8-8eef-4002-8f97-2d0c5df3c38d";

    @Mock private AuditLogService auditLogService;
    @Mock private ProceedingJoinPoint joinPoint;
    @Mock private Signature signature;
    @Mock private Audit audit;

    private AuditAspect auditAspect;

    private StpLogic originalLogic;

    @BeforeEach
    void setUp() {
        auditAspect = new AuditAspect(auditLogService);

        // 模拟当前请求由 STAFF 登录发起。
        originalLogic = StpUtil.getStpLogic();
        StpUtil.setStpLogic(new StpLogic(originalLogic.getLoginType()) {
            @Override public boolean isLogin() { return true; }
            @Override public String getLoginIdAsString() { return STAFF_ID; }
        });

        // 真实 HTTP 请求中由 Spring 设置；这里测试线程手动准备请求上下文。
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(audit.value()).thenReturn("工单已受理");
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        if (originalLogic != null)
            StpUtil.setStpLogic(originalLogic);
    }

    @Test
    @DisplayName("被审计业务成功：返回原结果，并记录 success=true")
    void success_shouldWriteSuccessAudit() throws Throwable {
        // Arrange：模拟被拦截的业务方法返回结果。
        when(joinPoint.proceed()).thenReturn("接单成功");

        // Act：直接执行真正的 AuditAspect 环绕通知。
        Object result = auditAspect.Around(joinPoint, audit);

        // Assert：结果不被切面更改，审计日志内容正确。
        assertEquals("接单成功", result);

        verify(joinPoint).proceed();
        verify(auditLogService).saveLog(
                eq(STAFF_ID), eq("工单已受理"), eq(true), isNull(),
                anyLong(), eq("127.0.0.1"), same(joinPoint));
    }

    @Test
    @DisplayName("被审计业务失败：记录 success=false，原业务异常继续抛出")
    void failure_shouldWriteFailureAuditAndRethrow() throws Throwable {
        // Arrange：模拟业务方法抛出异常。
        BusinessException expected = new BusinessException("工单不存在或已被处理");
        when(joinPoint.proceed()).thenThrow(expected);

        // Act + Assert：切面不能把原本的业务异常吞掉。
        BusinessException caught = assertThrows(
                BusinessException.class,
                () -> auditAspect.Around(joinPoint, audit)
        );
        assertSame(expected, caught);

        verify(joinPoint).proceed();
        verify(auditLogService).saveLog(
                eq(STAFF_ID), eq("工单已受理"), eq(false),
                eq("工单不存在或已被处理"), anyLong(),
                eq("127.0.0.1"), same(joinPoint));
    }
}
