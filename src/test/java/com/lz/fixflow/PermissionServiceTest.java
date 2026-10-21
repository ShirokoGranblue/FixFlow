package com.lz.fixflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lz.fixflow.common.BusinessException;
import com.lz.fixflow.entity.Ticket;
import com.lz.fixflow.entity.User;
import com.lz.fixflow.mapper.UserMapper;
import com.lz.fixflow.service.impl.PermissionServiceImpl;

/**
 * PermissionServiceTest
 */
@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock private UserMapper userMapper;
    @InjectMocks private PermissionServiceImpl permissionServiceImpl;

    Ticket ticket;
    User admin;
    User other;
    String ticketId = UUID.randomUUID().toString();
    String creatorId = UUID.randomUUID().toString();
    String assigneeId = UUID.randomUUID().toString();
    String adminId = UUID.randomUUID().toString();
    String otherId = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {

        ticket = new Ticket();
        admin = new User();
        other = new User();

        ticket.setId(ticketId);
        ticket.setCreatorId(creatorId);
        ticket.setAssigneeId(assigneeId);

        admin.setId(adminId);
        admin.setRole("ADMIN");

        other.setId(otherId);
        other.setRole("USER");
    }

    @Test
    void creatorCanAccessTicket(){
        assertDoesNotThrow(
            ()->permissionServiceImpl.requireTicketAccess(creatorId, ticket)
        );
        verifyNoInteractions(userMapper);
    }

    @Test
    void assigneeCanAccessTicket(){
        assertDoesNotThrow(
            ()->permissionServiceImpl.requireTicketAccess(assigneeId, ticket)
        );
        verifyNoInteractions(userMapper);
    }

    @Test
    void adminCanAccessTicket(){
        when(userMapper.selectById(adminId)).thenReturn(admin);
        assertDoesNotThrow(
            ()->permissionServiceImpl.requireTicketAccess(adminId, ticket)
        );
        verify(userMapper).selectById(adminId);
    }

    @Test
    void otherCanNotAccessTicket(){
        when(userMapper.selectById(otherId)).thenReturn(other);
        assertThrows(BusinessException.class, ()->{
            permissionServiceImpl.requireTicketAccess(otherId, ticket);
        });
        verify(userMapper).selectById(otherId);
    }
}