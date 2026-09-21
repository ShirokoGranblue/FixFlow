package com.lz.fixflow.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TicketVO {

    private String id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private String creatorId;
    private String assigneeId;
    private LocalDateTime createdAt;
}
