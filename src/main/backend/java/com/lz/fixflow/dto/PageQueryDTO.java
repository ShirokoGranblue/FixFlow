package com.lz.fixflow.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageQueryDTO {

    @Min(value = 1, message = "页码不能小于1")
    private long current = 1;

    @Min(value = 1, message = "范围为1-100")
    @Max(value = 100, message = "范围为1-100")
    private long pageSize = 10;
}
