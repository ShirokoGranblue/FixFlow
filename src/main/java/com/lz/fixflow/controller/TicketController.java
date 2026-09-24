package com.lz.fixflow.controller;

import com.lz.fixflow.common.PageResult;
import com.lz.fixflow.common.Result;
import com.lz.fixflow.dto.TicketAssignDTO;
import com.lz.fixflow.dto.TicketCommentDTO;
import com.lz.fixflow.dto.TicketCreateDTO;
import com.lz.fixflow.dto.TicketQueryDTO;
import com.lz.fixflow.entity.Ticket;
import com.lz.fixflow.service.CommentService;
import com.lz.fixflow.service.TicketService;
import com.lz.fixflow.vo.TicketCommentVO;
import com.lz.fixflow.vo.TicketHistoryVO;
import com.lz.fixflow.vo.TicketVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ticket")
public class TicketController {

    private final TicketService ticketService;
    private final CommentService commentService;

    @PostMapping("/create")
    public Result<TicketVO> createTicket(@Valid @RequestBody TicketCreateDTO dto) {
        return Result.success(ticketService.createTicket(dto));
    }

    @GetMapping("/my")
    public Result<List<TicketVO>> showTickets() {
        return Result.success(ticketService.showTickets());
    }

    @GetMapping("/{id}")
    public Result<TicketVO> showTicket(@PathVariable String id) {
        return Result.success(ticketService.showTicket(id));
    }

    @GetMapping("/open")
    public Result<List<TicketVO>> openTickets() {
        return Result.success(ticketService.getOpenTickets());
    }

    @PostMapping("/{id}/handle")
    public Result<TicketVO> handleTicket(@PathVariable String id) {
        return Result.success(ticketService.handleTicket(id));
    }

    @GetMapping("/assign")
    public Result<List<TicketVO>> assignTickets() {
        return Result.success(ticketService.getAssignedTickets());
    }

    @PostMapping("/{id}/resolve")
    public Result<TicketVO> resolveTicket(@PathVariable String id) {
        return Result.success(ticketService.resolveTicket(id));
    }

    @PostMapping("/{id}/close")
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
    public Result<PageResult<TicketVO>> manageTickets(TicketQueryDTO dto) {
        return Result.success(ticketService.searchTickets(dto));
    }

    @PostMapping("/{id}/assign")
    public Result<TicketVO> assignTickets(@PathVariable String id,
                                          @Valid @RequestBody TicketAssignDTO dto) {
        return Result.success(ticketService.assignTicket(id,dto));
    }

    @PostMapping("/{id}/cancel")
    public Result<TicketVO> cancelTickets(@PathVariable String id) {
        return Result.success(ticketService.cancelTicket(id));
    }
}
