package com.lz.fixflow.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data 
public class UserQueryDTO {
    
    @Min(value = 1)
    private long current = 1;

    @Min(value = 0 , message = "范围为1-100")
    @Max(value = 100 , message = "范围为1-100")
    private long pageSize = 10;

    private String keyword;

    private String role;
}
