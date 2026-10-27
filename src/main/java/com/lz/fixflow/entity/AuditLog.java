package com.lz.fixflow.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("audit_log")
public class AuditLog {

    @TableId
    private Long id;

    private String username;
    
    @TableField("user_id")
    private String userId;

    private String action;
    private String method;
    private Boolean success;
    private String errorMessage;
    private Long durationMs;
    private LocalDateTime createdAt;
    private String ip;
}
