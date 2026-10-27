package com.lz.fixflow.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data 
public class TicketListItemVO {
    private String id;
    private String title;
    private String status;
    private LocalDateTime created_at;
}
