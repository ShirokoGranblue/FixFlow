package com.lz.fixflow.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserVO {

    private String id;
    private String username;
    private String role;
    private String email;
    private LocalDateTime createdAt;
}
