package com.lz.fixflow;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lz.fixflow.entity.AuditLog;
import com.lz.fixflow.entity.User;
import com.lz.fixflow.mapper.AuditLogMapper;
import com.lz.fixflow.mapper.UserMapper;
import com.lz.fixflow.service.impl.AuditLogServiceImpl;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {
    @Mock AuditLogMapper auditMapper;
    @Mock UserMapper userMapper;
    @Mock ProceedingJoinPoint joinPoint;
    @Mock Signature signature;
    private AuditLogServiceImpl service;

    @BeforeEach void setUp() { service = new AuditLogServiceImpl(auditMapper, userMapper); }

    @Test
    void paginationFieldsAreNotSwapped() {
        when(auditMapper.selectPage(any(Page.class), any(Wrapper.class)))
                .thenAnswer(invocation -> {
                    Page<AuditLog> page = invocation.getArgument(0);
                    page.setRecords(List.of(new AuditLog()));
                    page.setTotal(41);
                    return page;
                });
        var result = service.getAuditLog(2, 20);
        assertEquals(2, result.getCurrent());
        assertEquals(20, result.getPageSize());
        assertEquals(3, result.getPages());
    }

    @Test
    void anonymousAuditDoesNotQueryNullUser() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("login(..)");
        service.recordAudit(null, "登录失败", false, "用户名或密码错误", 12,
                "127.0.0.1", joinPoint);
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditMapper).insert(captor.capture());
        assertNull(captor.getValue().getUserId());
        assertEquals("匿名用户", captor.getValue().getUsername());
        assertFalse(captor.getValue().getSuccess());
        verifyNoInteractions(userMapper);
    }

    @Test
    void loggedInAuditRetainsUsername() {
        User creator = new User();
        creator.setUsername("test-staff");
        when(userMapper.selectById("staff-id")).thenReturn(creator);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("close(..)");
        service.recordAudit("staff-id", "关闭工单", true, null, 10,
                "127.0.0.1", joinPoint);
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditMapper).insert(captor.capture());
        assertEquals("test-staff", captor.getValue().getUsername());
    }
}
