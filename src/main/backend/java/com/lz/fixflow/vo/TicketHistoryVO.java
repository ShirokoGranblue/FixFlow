package com.lz.fixflow.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketHistoryVO {
    private String id;
    private String operatorid;
    private String action;
    private String fromStatus;
    private String toStatus;
    private LocalDateTime createdAt;
}
