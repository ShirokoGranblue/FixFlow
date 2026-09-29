package com.lz.fixflow.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ticket_history")
public class TicketHistory {

    @TableId
    private String id;

    private String ticketId;
    private String operatorId;
    private String action;
    private String fromStatus;
    private String toStatus;
    private LocalDateTime createdAt;
}
