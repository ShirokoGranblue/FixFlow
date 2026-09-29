package com.lz.fixflow.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketCommentVO {

    private String id;
    private String content;
    private String authorId;
    private String authorName;
    private String ticketId;
    private LocalDateTime createdAt;
}
