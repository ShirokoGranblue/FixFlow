package com.lz.fixflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketCreateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 100,message = "标题不超过个100字符")
    private String title;

    @NotBlank(message = "描述不能为空")
    @Size(min=2,message = "描述不少于两个字符")
    private String description;

    @Pattern(regexp = "LOW|MEDIUM|HIGH|URGENT",message = "优先级不存在")
    private String priority;
}
