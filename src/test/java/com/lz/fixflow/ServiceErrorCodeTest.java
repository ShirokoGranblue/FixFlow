package com.lz.fixflow;

import com.lz.fixflow.common.BusinessException;
import com.lz.fixflow.common.ErrorCode;
import com.lz.fixflow.dto.UserLoginDTO;
import com.lz.fixflow.dto.UserRegisterDTO;
import com.lz.fixflow.entity.Ticket;
import com.lz.fixflow.entity.User;
import com.lz.fixflow.mapper.TicketMapper;
import com.lz.fixflow.mapper.UserMapper;
import com.lz.fixflow.service.PermissionService;
import com.lz.fixflow.service.impl.PermissionServiceImpl;
import com.lz.fixflow.service.impl.TicketCursorServiceImpl;
import com.lz.fixflow.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ServiceErrorCodeTest {
    private final UserMapper users = mock(UserMapper.class);
    private final PasswordEncoder passwords = mock(PasswordEncoder.class);
    private final UserServiceImpl service = new UserServiceImpl(users, passwords, mock(PermissionService.class));

    @Test
    void unknownUserAndWrongPasswordHaveSameCredentialError() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setUsername("tester");
        dto.setPassword("test-only-password");

        BusinessException missing = assertThrows(BusinessException.class, () -> service.login(dto));
        User user = new User();
        when(users.selectOne(any())).thenReturn(user);
        BusinessException incorrect = assertThrows(BusinessException.class, () -> service.login(dto));

        assertEquals(ErrorCode.INVALID_CREDENTIALS, missing.getErrorCode());
        assertEquals(ErrorCode.INVALID_CREDENTIALS, incorrect.getErrorCode());
        assertEquals(missing.getMessage(), incorrect.getMessage());
    }

    @Test
    void duplicateUsernameHasDistinctConflictCode() {
        when(users.selectCount(any())).thenReturn(1L);
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("tester");
        BusinessException failure = assertThrows(BusinessException.class, () -> service.register(dto));
        assertEquals(ErrorCode.USERNAME_EXISTS, failure.getErrorCode());
    }

    @Test
    void missingTicketAndDeniedAccessHaveDifferentCodes() {
        PermissionServiceImpl permissions = new PermissionServiceImpl(users);
        BusinessException missing = assertThrows(BusinessException.class,
                () -> permissions.requireTicketAccess("other", null));
        Ticket ticket = new Ticket();
        ticket.setCreatorId("creator");
        User user = new User();
        user.setRole("USER");
        when(users.selectById("other")).thenReturn(user);
        BusinessException forbidden = assertThrows(BusinessException.class,
                () -> permissions.requireTicketAccess("other", ticket));

        assertEquals(ErrorCode.NOT_FOUND, missing.getErrorCode());
        assertEquals(ErrorCode.FORBIDDEN, forbidden.getErrorCode());
    }

    @Test
    void invalidCursorUsesBusinessValidationError() {
        TicketMapper tickets = mock(TicketMapper.class);
        TicketCursorServiceImpl cursor = new TicketCursorServiceImpl(tickets);
        BusinessException size = assertThrows(BusinessException.class,
                () -> cursor.queryOpenTicketByCursor(0, null, null));
        BusinessException incomplete = assertThrows(BusinessException.class,
                () -> cursor.queryOpenTicketByCursor(10, LocalDateTime.now(), null));

        assertEquals(ErrorCode.INVALID_ARGUMENT, size.getErrorCode());
        assertEquals(ErrorCode.INVALID_ARGUMENT, incomplete.getErrorCode());
        verifyNoInteractions(tickets);
    }
}
