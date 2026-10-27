package com.lz.fixflow.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data 
public class TicketCursor {
    private String id;
    private LocalDateTime created_at;
}
