package com.lz.fixflow.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.lz.fixflow.common.PageResult;
import com.lz.fixflow.common.Result;
import com.lz.fixflow.dto.CursorPageResponseDTO;
import com.lz.fixflow.dto.TicketAssignDTO;
import com.lz.fixflow.dto.TicketCommentDTO;
import com.lz.fixflow.dto.TicketCreateDTO;
import com.lz.fixflow.dto.TicketQueryDTO;
import com.lz.fixflow.service.CommentService;
import com.lz.fixflow.service.TicketCursorService;
import com.lz.fixflow.service.TicketService;
import com.lz.fixflow.vo.TicketCommentVO;
import com.lz.fixflow.vo.TicketDetailVO;
import com.lz.fixflow.vo.TicketHistoryVO;
import com.lz.fixflow.vo.TicketListItemVO;
import com.lz.fixflow.vo.TicketVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ticket")
public class TicketController {

    private final TicketService ticketService;
    private final CommentService commentService;
    private final TicketCursorService ticketCursorService;

    @PostMapping("/create")
    public Result<TicketVO> createTicket(@Valid @RequestBody TicketCreateDTO dto) {
        return Result.success(ticketService.createTicket(dto));
    }

    @GetMapping("/my-tickets")
    public Result<List<TicketVO>> showTickets() {
        return Result.success(ticketService.showTickets());
    }

    @GetMapping("/{id}")
    public Result<TicketVO> showTicket(@PathVariable String id) {
        return Result.success(ticketService.showTicket(id));
    }

    @GetMapping("/open")
    @SaCheckRole(value = {"STAFF","ADMIN"}, mode = SaMode.OR)
    public Result<List<TicketVO>> getOpenTickets() {
        return Result.success(ticketService.getOpenTickets());
    }

    @PostMapping("/{id}/reopen")
    public Result<TicketVO> reOpenTicket(@PathVariable String id) {
        return Result.success(ticketService.reOpenTicket(id));
    }

    @PostMapping("/{id}/handle")
    @SaCheckRole(value = {"STAFF","ADMIN"},mode=SaMode.OR)
    public Result<TicketVO> handleTicket(@PathVariable String id) {
        return Result.success(ticketService.handleTicket(id));
    }

    @GetMapping("/assigned")
    @SaCheckRole(value = {"ADMIN","STAFF"},mode=SaMode.OR)
    public Result<List<TicketVO>> getAssignTickets() {
        return Result.success(ticketService.getAssignedTickets());
    }

    @PostMapping("/{id}/resolve")
    @SaCheckRole(value = {"STAFF","ADMIN"},mode=SaMode.OR)
    public Result<TicketVO> resolveTicket(@PathVariable String id) {
        return Result.success(ticketService.resolveTicket(id));
    }

    @PostMapping("/{id}/close")
    @SaCheckRole(value = {"STAFF","ADMIN"},mode = SaMode.OR)
    public Result<TicketVO> closeTicket(@PathVariable String id) {
        return Result.success(ticketService.closeTicket(id));
    }

    @PostMapping("/{id}/comment")
    public Result<TicketCommentVO> commentTicket(@Valid @RequestBody TicketCommentDTO dto, @PathVariable String id) {
        return Result.success(commentService.addComment(id,dto));
    }

    @GetMapping("/{id}/comment")
    public Result<List<TicketCommentVO>> getComment(@PathVariable String id) {
        return Result.success(commentService.getComment(id));
    }

    @GetMapping("/{id}/history")
    public Result<List<TicketHistoryVO>> getTicketHistory(@PathVariable String id) {
        return Result.success(ticketService.getTicketHistory(id));
    }

    @GetMapping("/manage")
    @SaCheckRole(value = {"STAFF", "ADMIN"}, mode = SaMode.OR)
    public Result<PageResult<TicketVO>> searchTickets(@Valid TicketQueryDTO dto) {
        return Result.success(ticketService.searchTickets(dto));
    }

    @PostMapping("/{id}/assign")
    @SaCheckRole(value = "ADMIN")
    public Result<TicketVO> assignTickets(@PathVariable String id,
                                          @Valid @RequestBody TicketAssignDTO dto) {
        return Result.success(ticketService.assignTicket(id,dto));
    }

    @PostMapping("/{id}/cancel")
    public Result<TicketVO> cancelTickets(@PathVariable String id) {
        return Result.success(ticketService.cancelTicket(id));
    }

    @GetMapping("/{id}/detail")
    public Result<TicketDetailVO> getTicketDetail(@PathVariable String id) {
        return  Result.success(ticketService.getTicketDetail(id));
    }

    @GetMapping("/cursor")
    @SaCheckRole(value = {"STAFF","ADMIN"},mode=SaMode.OR)
    public Result<CursorPageResponseDTO<TicketListItemVO>> queryCursorPageResponseDTO(
                                                    @RequestParam(defaultValue = "20") int size,

                                                    @RequestParam(required = false)
                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)    
                                                    LocalDateTime cursorTime,

                                                    @RequestParam(required = false) String cursorId) {

        return Result.success(
            ticketCursorService.queryOpenTicketByCursor(size, 
                                                        cursorTime, 
                                                        cursorId));
    }
}
