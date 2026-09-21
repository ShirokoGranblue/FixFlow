package com.lz.fixflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@TableName("ticket")
@Data
public class Ticket {

    @TableId(type = IdType.AUTO)
    private String id;

    private String title;
    private String description;
    private String priority;
    private String status;
    private String creatorId;
    private String assigneeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

}
