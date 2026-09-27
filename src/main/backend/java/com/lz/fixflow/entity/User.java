package com.lz.fixflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@TableName("user")
public class User {

    @TableId
    private String id;
    private String username;
    private String email;
    private String role;
    private String password;
    private LocalDateTime createdAt;
}
