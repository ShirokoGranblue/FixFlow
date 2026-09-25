package com.lz.fixflow.dto;

import lombok.Data;

@Data
public class TicketQueryDTO {

    private long current = 1;

    private long pageSize = 10;

    private String keyword;

    private String status;

    private String priority;

    private Long categoryId;

    private String sort;
}
