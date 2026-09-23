package com.lz.fixflow.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryVO {

    private Long id;
    private String name;
    private LocalDateTime createdAt;
}
