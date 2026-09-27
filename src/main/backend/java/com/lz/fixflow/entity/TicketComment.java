package com.lz.fixflow.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ticket_comment")
public class TicketComment {

    @TableId
    private String id;

    private String ticketId;
    private String authorId;
    private String content;
    private LocalDateTime createdAt;
}
