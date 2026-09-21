package com.lz.fixflow.controller;

import com.lz.fixflow.common.Result;
import com.lz.fixflow.dto.TicketCreateDTO;
import com.lz.fixflow.service.TicketService;
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

    @PostMapping("/{id}/handle")
    public Result<TicketVO> handleTickets(@PathVariable String id) {
        return Result.success(ticketService.handleTickets(id));
    }

    @GetMapping("/open")
    public Result<List<TicketVO>> openTickets() {
        return Result.success(ticketService.getOpenTickets());
    }
}
