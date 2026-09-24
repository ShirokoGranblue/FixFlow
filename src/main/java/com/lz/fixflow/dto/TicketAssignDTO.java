package com.lz.fixflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketAssignDTO {

    @NotBlank(message = "选择处理人员")
    private String assignId;
}
