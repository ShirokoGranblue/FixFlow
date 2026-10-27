package com.lz.fixflow;

import com.lz.fixflow.common.BusinessException;
import com.lz.fixflow.common.ErrorCode;
import com.lz.fixflow.common.GlobalExceptionHandler;
import com.lz.fixflow.common.Result;
import com.lz.fixflow.controller.UserController;
import com.lz.fixflow.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {
    private final UserService users = mock(UserService.class);
    private MockMvc mvc;
    private static final String LOGIN_BODY = "{\"username\":\"tester\",\"password\":\"test-pass-01\"}";

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new UserController(users), new ParameterController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ResultActions login() throws Exception {
        return mvc.perform(post("/api/user/login").contentType(MediaType.APPLICATION_JSON)
                .content(LOGIN_BODY));
    }

    private void assertError(ResultActions result, int httpStatus, int code) throws Exception {
        result.andExpect(status().is(httpStatus))
                .andExpect(jsonPath("$.httpStatus").value(httpStatus))
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @ParameterizedTest
    @EnumSource(ErrorCode.class)
    void businessErrorsKeepTheirHttpStatusAndBusinessCode(ErrorCode error) throws Exception {
        when(users.login(any())).thenThrow(new BusinessException(error));
        ResultActions result = login();
        assertError(result, error.getHttpStatus().value(), error.getCode());
        result.andExpect(jsonPath("$.message").value(error.getMessage()));
    }

    @Test
    void successIncludesHttpStatus() throws Exception {
        login().andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void legacyBusinessExceptionRetainsBusinessCode() throws Exception {
        when(users.login(any())).thenThrow(new BusinessException("业务请求不合法"));
        assertError(login(), 400, 4000);
    }

    @Test
    void responseStatusExceptionPreservesStatusReasonAndHeaders() throws Exception {
        ResponseStatusException failure = new ResponseStatusException(HttpStatus.CONFLICT, "状态冲突") {
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Retry-After", "5");
                return headers;
            }
        };
        when(users.login(any())).thenThrow(failure);
        ResultActions result = login();
        assertError(result, 409, 4090);
        result.andExpect(header().string("Retry-After", "5"))
                .andExpect(jsonPath("$.message").value("状态冲突"));
    }

    @Test
    void unexpectedFailureDoesNotExposeInternalMessage() throws Exception {
        when(users.login(any())).thenThrow(new IllegalStateException("internal diagnostic detail"));
        ResultActions result = login();
        assertError(result, 500, 5000);
        result.andExpect(jsonPath("$.message").value("服务器内部错误"));
    }

    @Test
    void serverResponseStatusExceptionPreservesStatusWithoutExposingReason() throws Exception {
        when(users.login(any())).thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "internal diagnostic detail"));
        ResultActions result = login();
        assertError(result, 503, 5000);
        result.andExpect(jsonPath("$.message").value("服务器内部错误"));
    }

    @Test
    void invalidDtoReturnsValidationBusinessCode() throws Exception {
        assertError(mvc.perform(post("/api/user/login").contentType(MediaType.APPLICATION_JSON)
                .content("{}")), 400, 4001);
        verifyNoInteractions(users);
    }

    @Test
    void malformedJsonReturns400InsteadOf500() throws Exception {
        assertError(mvc.perform(post("/api/user/login").contentType(MediaType.APPLICATION_JSON)
                .content("{")), 400, 4001);
        verifyNoInteractions(users);
    }

    @Test
    void unsupportedMethodPreservesAllowHeader() throws Exception {
        ResultActions result = mvc.perform(get("/api/user/login"));
        assertError(result, 405, 4050);
        result.andExpect(header().string("Allow", containsString("POST")));
    }

    @Test
    void unsupportedContentTypeReturns415() throws Exception {
        assertError(mvc.perform(post("/api/user/login").contentType(MediaType.TEXT_PLAIN)
                .content(LOGIN_BODY)), 415, 4150);
    }

    @Test
    void missingAndMalformedParametersReturn400() throws Exception {
        assertError(mvc.perform(get("/test/parameter")), 400, 4001);
        assertError(mvc.perform(get("/test/parameter").param("size", "abc")), 400, 4001);
    }

    @Test
    void unknownRouteReturns404() throws Exception {
        assertError(mvc.perform(get("/test/missing")), 404, 4040);
    }

    @RestController
    static class ParameterController {
        @GetMapping("/test/parameter")
        Result<Integer> parameter(@RequestParam("size") int size) {
            return Result.success(size);
        }
    }
}
