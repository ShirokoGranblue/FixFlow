package com.lz.fixflow.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketVO {

    private String id;

    private String title;

    private String description;

    private String priority;

    private String status;

    private String creatorId;

    private String assigneeId;

    private Long categoryId;

    private String categoryName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;
}
