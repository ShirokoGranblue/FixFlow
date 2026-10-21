package com.lz.fixflow;

import cn.dev33.satoken.stp.StpUtil;
import com.lz.fixflow.entity.Category;
import com.lz.fixflow.entity.Ticket;
import com.lz.fixflow.entity.User;
import com.lz.fixflow.enums.TicketPriority;
import com.lz.fixflow.enums.TicketStatus;
import com.lz.fixflow.enums.UserRole;
import com.lz.fixflow.mapper.CategoryMapper;
import com.lz.fixflow.mapper.TicketCommentMapper;
import com.lz.fixflow.mapper.TicketHistoryMapper;
import com.lz.fixflow.mapper.TicketMapper;
import com.lz.fixflow.mapper.UserMapper;
import com.lz.fixflow.service.AuditLogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TicketResourceAccessHttpTest {

    private static final String TICKET_ID = "ticket-001";
    private static final String CREATOR_ID = "user-creator";
    private static final String ASSIGNEE_ID = "user-staff";
    private static final String OTHER_ID = "user-other";
    private static final String ADMIN_ID = "user-admin";
    private static final String ANY_ID = "user-any";
    private static final long CATEGORY_ID = 1L;

    @Autowired
    private MockMvc mvc;

    // Mapper 全部换成 Mockito，避免这个测试真的读写业务表。
    // Controller、TicketServiceImpl、PermissionServiceImpl 仍然是真实 Spring Bean。
    @MockitoBean private TicketMapper ticketMapper;
    @MockitoBean private TicketCommentMapper ticketCommentMapper;
    @MockitoBean private TicketHistoryMapper ticketHistoryMapper;
    @MockitoBean private CategoryMapper categoryMapper;
    @MockitoBean private UserMapper userMapper;

    // showTicket 上有 @Audit。这里不测试审计持久化，所以把审计服务 Mock 掉。
    @MockitoBean private AuditLogService auditLogService;

    private final List<String> tokens = new ArrayList<>();
    private Ticket ticket;
    private Category category;

    @BeforeEach
    void setUp() {
        ticket = new Ticket();
        ticket.setId(TICKET_ID);
        ticket.setTitle("公司网络无法访问");
        ticket.setDescription("办公电脑无法连接公司网络");
        ticket.setPriority(TicketPriority.HIGH.getValue());
        ticket.setStatus(TicketStatus.IN_PROGRESS.getValue());
        ticket.setCreatorId(CREATOR_ID);
        ticket.setAssigneeId(ASSIGNEE_ID);
        ticket.setCategoryId(CATEGORY_ID);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        category = new Category();
        category.setId(CATEGORY_ID);
        category.setName("网络问题");
    }

    @AfterEach
    void clearLoginSessions() {
        tokens.forEach(StpUtil::logoutByTokenValue);
    }

    private String login(String userId) {
        String token = StpUtil.createLoginSession(userId);
        tokens.add(token);
        return token;
    }

    private User user(String id, String role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }

    @Test
    void creatorCanReadOwnTicketThroughHttp() throws Exception {
        when(ticketMapper.selectById(TICKET_ID)).thenReturn(ticket);
        when(categoryMapper.selectById(CATEGORY_ID)).thenReturn(category);

        String token = login(CREATOR_ID);

        mvc.perform(get("/api/ticket/{id}", TICKET_ID)
                    .header(StpUtil.getTokenName(), token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(TICKET_ID))
                    .andExpect(jsonPath("$.data.creatorId").value(CREATOR_ID))
                    .andExpect(jsonPath("$.data.categoryName").value("网络问题"));

        // 创建者在 PermissionService 中直接放行，不需要查角色。
        verifyNoInteractions(userMapper);
    }

    @Test
    void assigneeCanReadAssignedTicketThroughHttp() throws Exception {
        when(ticketMapper.selectById(TICKET_ID)).thenReturn(ticket);
        when(categoryMapper.selectById(CATEGORY_ID)).thenReturn(category);

        String token = login(ASSIGNEE_ID);

        mvc.perform(get("/api/ticket/{id}", TICKET_ID)
                    .header(StpUtil.getTokenName(), token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.assigneeId").value(ASSIGNEE_ID));

        // 处理人同样直接放行，不需要额外查用户角色。
        verifyNoInteractions(userMapper);
    }

    @Test
    void adminCanReadUnrelatedTicketThroughHttp() throws Exception {
        when(ticketMapper.selectById(TICKET_ID)).thenReturn(ticket);
        when(userMapper.selectById(ADMIN_ID))
                .thenReturn(user(ADMIN_ID, UserRole.ADMIN.getValue()));
        when(categoryMapper.selectById(CATEGORY_ID)).thenReturn(category);

        String token = login(ADMIN_ID);

        mvc.perform(get("/api/ticket/{id}", TICKET_ID)
                        .header(StpUtil.getTokenName(), token))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200))
                        .andExpect(jsonPath("$.data.id").value(TICKET_ID));

        verify(userMapper).selectById(ADMIN_ID);
    }

    @Test
    void unrelatedUserCannotReadAnotherUsersTicketThroughHttp() throws Exception {
        when(ticketMapper.selectById(TICKET_ID)).thenReturn(ticket);
        when(userMapper.selectById(OTHER_ID))
                .thenReturn(user(OTHER_ID, UserRole.USER.getValue()));

        String token = login(OTHER_ID);

        mvc.perform(get("/api/ticket/{id}", TICKET_ID)
                        .header(StpUtil.getTokenName(), token))
                        // 当前 GlobalExceptionHandler 把业务错误包装为 JSON，HTTP 状态仍为 200。
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(4000))
                        .andExpect(jsonPath("$.message").value("无权访问工单"));

        // 权限校验已经失败，所以后面的分类查询不应该发生。
        verify(categoryMapper, never()).selectById(anyLong());
    }

    @Test
    void missingTicketReturnsBusinessErrorThroughHttp() throws Exception {
        when(ticketMapper.selectById(TICKET_ID)).thenReturn(null);

        String token = login(ANY_ID);

        mvc.perform(get("/api/ticket/{id}", TICKET_ID)
                        .header(StpUtil.getTokenName(), token))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(4000))
                        .andExpect(jsonPath("$.message").value("工单不存在"));

        verify(categoryMapper, never()).selectById(anyLong());
    }
}
