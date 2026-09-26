package com.lz.fixflow;

import cn.dev33.satoken.stp.StpUtil;
import com.lz.fixflow.entity.User;
import com.lz.fixflow.mapper.UserMapper;
import com.lz.fixflow.service.CategoryService;
import com.lz.fixflow.service.CommentService;
import com.lz.fixflow.service.DashBoardService;
import com.lz.fixflow.service.TicketService;
import com.lz.fixflow.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserMapper users;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private TicketService tickets;
    @MockitoBean
    private CommentService comments;
    @MockitoBean
    private DashBoardService dashboard;
    @MockitoBean
    private CategoryService categories;

    private final List<String> tokens = new ArrayList<>();

    private record Endpoint(String method, String path, String body, boolean adminOnly) {
        MockHttpServletRequestBuilder toRequest() {
            return request(HttpMethod.valueOf(method), path)
                    .contentType(MediaType.APPLICATION_JSON).content(body);
        }
    }

    private static Stream<Endpoint> roleEndpoints() {
        return Stream.of(
                new Endpoint("GET", "/api/dashboard/", "", false),
                new Endpoint("GET", "/api/ticket/open", "", false),
                new Endpoint("GET", "/api/ticket/assign", "", false),
                new Endpoint("GET", "/api/ticket/manage", "", false),
                new Endpoint("POST", "/api/ticket/ticket-id/handle", "", false),
                new Endpoint("POST", "/api/ticket/ticket-id/resolve", "", false),
                new Endpoint("POST", "/api/ticket/ticket-id/close", "", false),
                new Endpoint("POST", "/api/ticket/ticket-id/assign", "{\"assignId\":\"staff-id\"}", true),
                new Endpoint("GET", "/api/admin/user/staff", "", true),
                new Endpoint("POST", "/api/admin/user/user-id/role", "{\"role\":\"STAFF\"}", true)
        );
    }

    private static Stream<Endpoint> loginOnlyEndpoints() {
        return Stream.of(
                new Endpoint("GET", "/api/category/", "", false),
                new Endpoint("GET", "/api/ticket/my", "", false),
                new Endpoint("GET", "/api/ticket/ticket-id", "", false),
                new Endpoint("POST", "/api/ticket/create",
                        "{\"title\":\"测试工单\",\"description\":\"测试描述\",\"categoryId\":1}", false),
                new Endpoint("POST", "/api/ticket/ticket-id/reopen", "", false),
                new Endpoint("POST", "/api/ticket/ticket-id/cancel", "", false),
                new Endpoint("POST", "/api/ticket/ticket-id/detail", "", false),
                new Endpoint("POST", "/api/ticket/ticket-id/comment", "{\"content\":\"测试评论\"}", false),
                new Endpoint("GET", "/api/ticket/ticket-id/comment", "", false),
                new Endpoint("GET", "/api/ticket/ticket-id/history", "", false)
        );
    }

    private static Stream<Endpoint> protectedEndpoints() {
        return Stream.concat(loginOnlyEndpoints(), roleEndpoints());
    }

    private static Stream<Arguments> roleAccessCases() {
        return roleEndpoints().flatMap(endpoint -> Stream.of("USER", "STAFF", "ADMIN")
                .map(role -> Arguments.of(endpoint, role)));
    }

    private String login(User user) {
        when(users.selectById(user.getId())).thenReturn(user);
        String token = StpUtil.createLoginSession(user.getId());
        tokens.add(token);
        return token;
    }

    private User account(String role) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setRole(role);
        return user;
    }

    private void assertNoBusinessCalls() {
        verifyNoInteractions(userService, tickets, comments, dashboard, categories);
    }

    private long businessCallCount() {
        return Stream.of(userService, tickets, comments, dashboard, categories)
                .mapToLong(service -> mockingDetails(service).getInvocations().size()).sum();
    }

    @AfterEach
    void logoutTestSessions() {
        tokens.forEach(StpUtil::logoutByTokenValue);
    }

    @Test
    void loginIsPublic() throws Exception {
        mvc.perform(post("/api/user/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test-user\",\"password\":\"test-only-pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).login(any());
        verifyNoInteractions(users);
    }

    @Test
    void registrationIsPublic() throws Exception {
        mvc.perform(post("/api/user/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test-user\",\"password\":\"test-only-pass\",\"email\":\"test@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).register(any());
        verifyNoInteractions(users);
    }

    @ParameterizedTest
    @MethodSource("protectedEndpoints")
    void anonymousRequestsAreRejectedBeforeBusinessLogic(Endpoint endpoint) throws Exception {
        mvc.perform(endpoint.toRequest())
                .andExpect(jsonPath("$.code").value(4010))
                .andExpect(jsonPath("$.message").value("请先登录"));

        assertNoBusinessCalls();
        verifyNoInteractions(users);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/ticket/my", "/api/ticket/open", "/api/admin/user/staff"})
    void invalidTokensAreRejected(String path) throws Exception {
        mvc.perform(get(path).header(StpUtil.getTokenName(), "invalid-test-token"))
                .andExpect(jsonPath("$.code").value(4010));

        assertNoBusinessCalls();
        verifyNoInteractions(users);
    }

    @Test
    void loggedOutTokenIsRejected() throws Exception {
        String token = login(account("ADMIN"));
        StpUtil.logoutByTokenValue(token);

        mvc.perform(get("/api/admin/user/staff").header(StpUtil.getTokenName(), token))
                .andExpect(jsonPath("$.code").value(4010));

        assertNoBusinessCalls();
    }

    @ParameterizedTest
    @MethodSource("loginOnlyEndpoints")
    void ordinaryUserPassesLoginCheck(Endpoint endpoint) throws Exception {
        String token = login(account("USER"));

        mvc.perform(endpoint.toRequest().header(StpUtil.getTokenName(), token))
                .andExpect(jsonPath("$.code").value(200));

        assertEquals(1, businessCallCount());
    }

    @ParameterizedTest
    @MethodSource("roleAccessCases")
    void rolesAreCheckedAtHttpEntryPoint(Endpoint endpoint, String role) throws Exception {
        User user = account(role);
        String token = login(user);
        boolean allowed = "ADMIN".equals(role) || ("STAFF".equals(role) && !endpoint.adminOnly());

        mvc.perform(endpoint.toRequest().header(StpUtil.getTokenName(), token))
                .andExpect(jsonPath("$.code").value(allowed ? 200 : 4030));

        verify(users).selectById(user.getId());
        if (allowed) {
            assertEquals(1, businessCallCount());
        } else {
            assertNoBusinessCalls();
        }
    }

    @Test
    void deletedUserCannotPassRoleCheck() throws Exception {
        User user = account("ADMIN");
        String token = login(user);
        when(users.selectById(user.getId())).thenReturn(null);

        mvc.perform(get("/api/admin/user/staff").header(StpUtil.getTokenName(), token))
                .andExpect(jsonPath("$.code").value(4030));

        assertNoBusinessCalls();
    }

    @Test
    void roleRevocationTakesEffectForExistingToken() throws Exception {
        User user = account("ADMIN");
        String token = login(user);
        mvc.perform(get("/api/admin/user/staff").header(StpUtil.getTokenName(), token))
                .andExpect(jsonPath("$.code").value(200));
        verify(userService).getStaffUsers();
        clearInvocations(userService);

        user.setRole("USER");
        mvc.perform(get("/api/admin/user/staff").header(StpUtil.getTokenName(), token))
                .andExpect(jsonPath("$.code").value(4030));

        assertNoBusinessCalls();
    }
}
