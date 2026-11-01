package com.lz.fixflow;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lz.fixflow.common.BusinessException;
import com.lz.fixflow.dto.TicketAssignDTO;
import com.lz.fixflow.dto.TicketQueryDTO;
import com.lz.fixflow.entity.Ticket;
import com.lz.fixflow.entity.User;
import com.lz.fixflow.mapper.*;
import com.lz.fixflow.service.TicketHistoryService;
import com.lz.fixflow.service.impl.PermissionServiceImpl;
import com.lz.fixflow.service.impl.TicketServiceImpl;
import com.lz.fixflow.service.impl.UserServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
class TicketLogicTests {

    private final TicketMapper tickets = mock(TicketMapper.class);
    private final UserMapper users = mock(UserMapper.class);
    private final CategoryMapper categories = mock(CategoryMapper.class);
    private final TicketHistoryMapper histories = mock(TicketHistoryMapper.class);
    private final TicketHistoryService historyService = mock(TicketHistoryService.class);
    private final Map<String, User> accounts = new HashMap<>();
    private final PermissionServiceImpl permissions = new PermissionServiceImpl(users);
    private final TicketServiceImpl service = new TicketServiceImpl(
            tickets, mock(TicketCommentMapper.class),users, histories, categories, permissions,historyService);
    private final Ticket ticket = new Ticket();
    private StpLogic originalLogic;
    private String actor = "admin";

    @BeforeEach
    void setUp() {
        originalLogic = StpUtil.getStpLogic();
        StpUtil.setStpLogic(new StpLogic(originalLogic.getLoginType()) {
            @Override
            public String getLoginIdAsString() {
                return actor;
            }
        });
        for (Class<?> entity : List.of(Ticket.class, User.class)) {
            MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "test");
            assistant.setCurrentNamespace("test." + entity.getSimpleName());
            TableInfoHelper.initTableInfo(assistant, entity);
        }
        account("admin", "ADMIN");
        account("staff", "STAFF");
        account("creator", "USER");
        account("other", "USER");
        when(users.selectById(anyString())).thenAnswer(call -> accounts.get(call.getArgument(0)));
        ticket.setId("ticket-id");
        ticket.setCreatorId("creator");
        ticket.setCategoryId(1L);
        ticket.setStatus("OPEN");
        when(tickets.selectById(ticket.getId())).thenReturn(ticket);
    }

    @AfterEach
    void restoreLoginLogic() {
        StpUtil.setStpLogic(originalLogic);
    }

    private void account(String id, String role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        accounts.put(id, user);
    }

    @ParameterizedTest
    @ValueSource(strings = {"staff", "admin"})
    void adminCanAssignToEligibleUser(String assignee) {
        when(tickets.update(isNull(), any(Wrapper.class))).thenReturn(1);
        TicketAssignDTO dto = new TicketAssignDTO();
        dto.setAssignId(assignee);

        assertEquals(ticket.getId(), service.assignTicket(ticket.getId(), dto).getId());
        verify(tickets).update(isNull(), any(Wrapper.class));
        verify(historyService).recordHistory(ticket.getId(), "admin", "ASSIGN", "OPEN", "IN_PROGRESS");
    }

    @ParameterizedTest
    @ValueSource(strings = {"creator", "missing"})
    void invalidAssigneeIsRejectedBeforeUpdatingTicket(String assignee) {
        when(tickets.update(isNull(), any(Wrapper.class))).thenReturn(1);
        TicketAssignDTO dto = new TicketAssignDTO();
        dto.setAssignId(assignee);

        assertThrows(BusinessException.class, () -> service.assignTicket(ticket.getId(), dto));
        verify(tickets, never()).update(isNull(), any(Wrapper.class));
        verifyNoInteractions(historyService, histories);
    }

    @ParameterizedTest
    @ValueSource(strings = {"creator", "staff", "admin"})
    void participantsAndAdminCanReadHistory(String operator) {
        actor = operator;
        ticket.setAssigneeId("staff");
        when(histories.selectList(any(Wrapper.class))).thenReturn(List.of());

        assertTrue(service.getTicketHistory(ticket.getId()).isEmpty());
        verify(histories).selectList(any(Wrapper.class));
    }

    @Test
    void unrelatedUserCannotReadHistory() {
        actor = "other";

        assertThrows(BusinessException.class, () -> service.getTicketHistory(ticket.getId()));
        verifyNoInteractions(histories);
    }

    @Test
    void missingTicketHistoryIsRejected() {
        assertThrows(BusinessException.class, () -> service.getTicketHistory("missing"));
        verifyNoInteractions(histories);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"create_asc", "create_desc", "priority_asc", "priority_desc", "unknown"})
    void sortingUsesOneOrderByAndPreservesTicketId(String sort) {
        when(tickets.selectPage(any(Page.class), any(Wrapper.class))).thenAnswer(call -> {
            Page<Ticket> page = call.getArgument(0);
            Wrapper<Ticket> wrapper = call.getArgument(1);
            String sql = wrapper.getSqlSegment().replaceAll("\\s+", " ").trim();
            assertEquals(2, sql.split("ORDER BY", -1).length, sql);
            if ("priority_asc".equals(sort) || "priority_desc".equals(sort)) {
                assertTrue(sql.contains("CASE"), sql);
                assertTrue(sql.endsWith(sort.endsWith("asc") ? "ASC" : "DESC"), sql);
            } else {
                assertEquals("ORDER BY created_at " + ("create_asc".equals(sort) ? "ASC" : "DESC"), sql);
            }
            page.setRecords(List.of(ticket));
            page.setTotal(1);
            return page;
        });
        TicketQueryDTO dto = new TicketQueryDTO();
        dto.setSort(sort);

        assertEquals(ticket.getId(), service.searchTickets(dto).getRecords().getFirst().getId());
    }

    @Test
    void openQueueIncludesTicketsCreatedByOtherUsers() {
        when(tickets.selectList(any(Wrapper.class))).thenAnswer(call -> {
            Wrapper<Ticket> wrapper = call.getArgument(0);
            String sql = wrapper.getSqlSegment();
            assertFalse(sql.contains("creator_id"), sql);
            assertTrue(sql.contains("assignee_id IS NULL"), sql);
            return List.of(ticket);
        });

        assertEquals(ticket.getId(), service.getOpenTickets().getFirst().getId());
    }

    @Test
    void openTicketsBatchFetchCreatorsWithoutNPlusOne() {
        when(tickets.selectList(any(Wrapper.class))).thenReturn(List.of(ticket));
        when(users.selectBatchIds(anyCollection())).thenReturn(List.of(accounts.get("creator")));
        assertEquals(ticket.getId(), service.getOpenTickets().getFirst().getId());
        verify(users, times(1)).selectBatchIds(anyCollection());
        verify(users, never()).selectById(anyString());
    }

    @Test
    void paginatedOpenQueueKeepsPageMetadata() {
        when(tickets.selectPage(any(Page.class), any(Wrapper.class))).thenAnswer(inv -> {
            Page<Ticket> page = inv.getArgument(0);
            page.setRecords(List.of(ticket));
            page.setTotal(21);
            return page;
        });
        var result = service.getOpenTicketsPage(2, 10);
        assertEquals(1, result.getRecords().size());
        assertEquals(2, result.getCurrent());
        assertEquals(10, result.getPageSize());
        assertEquals(3, result.getPages());
    }

    @Test
    void creatorCanConfirmCloseAfterResolved() {
        actor = "creator";
        ticket.setStatus("RESOLVED");
        ticket.setAssigneeId("staff");
        when(tickets.update(isNull(), any(Wrapper.class))).thenReturn(1);
        assertEquals(ticket.getId(), service.closeTicket(ticket.getId()).getId());
        verify(historyService).recordHistory(ticket.getId(), "creator", "CLOSE", "RESOLVED", "CLOSED");
    }

    @Test
    void assigneeCannotConfirmCloseForCreator() {
        actor = "staff";
        ticket.setStatus("RESOLVED");
        ticket.setAssigneeId("staff");
        assertThrows(BusinessException.class, () -> service.closeTicket(ticket.getId()));
        verify(tickets, never()).update(isNull(), any(Wrapper.class));
        verifyNoInteractions(historyService);
    }

    @Test
    void closeConflictsWithConcurrentReopen() {
        actor = "creator";
        ticket.setStatus("RESOLVED");
        when(tickets.update(isNull(), any(Wrapper.class))).thenReturn(0);
        assertThrows(BusinessException.class, () -> service.closeTicket(ticket.getId()));
        verifyNoInteractions(historyService);
    }

    @Test
    void detailPreservesNullAndHistoricalResolvedTime() {
        assertNull(service.showTicket(ticket.getId()).getResolvedAt());
        LocalDateTime resolved = LocalDateTime.of(2026, 9, 1, 12, 0);
        ticket.setResolvedAt(resolved);
        assertEquals(resolved, service.showTicket(ticket.getId()).getResolvedAt());
    }

    @Test
    void staffListReturnsStoredUserIds() {
        when(users.selectList(any(Wrapper.class))).thenReturn(List.of(accounts.get("staff")));
        UserServiceImpl userService = new UserServiceImpl(users, null, permissions);

        assertEquals("staff", userService.getStaffUsers().getFirst().getId());
    }
}
