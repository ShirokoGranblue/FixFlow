package com.lz.fixflow;

import com.lz.fixflow.common.GlobalExceptionHandler;
import com.lz.fixflow.common.PageResult;
import com.lz.fixflow.controller.TicketController;
import com.lz.fixflow.dto.TicketQueryDTO;
import com.lz.fixflow.service.CommentService;
import com.lz.fixflow.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TicketQueryValidationTests {

    private final TicketService service = mock(TicketService.class);
    private final CommentService commentService = mock(CommentService.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new TicketController(service, commentService, null))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        when(service.searchTickets(any())).thenReturn(new PageResult<>(List.of(), 0, 1, 10, 0));
    }

    @ParameterizedTest
    @CsvSource({"-1,1", "0,1", "101,1", "10,0", "10,-1"})
    void invalidPaginationIsRejectedBeforeService(long size, long current) throws Exception {
        mvc.perform(get("/api/ticket/manage")
                        .param("pageSize", Long.toString(size))
                        .param("current", Long.toString(current)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpStatus").value(400))
                .andExpect(jsonPath("$.code").value(4001));

        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @CsvSource({"1,1", "100,1", "10,5"})
    void validPaginationReachesService(long size, long current) throws Exception {
        mvc.perform(get("/api/ticket/manage")
                        .param("pageSize", Long.toString(size))
                        .param("current", Long.toString(current)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(service).searchTickets(any(TicketQueryDTO.class));
    }

    @Test
    void defaultPaginationIsValid() throws Exception {
        mvc.perform(get("/api/ticket/manage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
