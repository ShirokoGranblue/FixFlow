package com.lz.fixflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketCommentDTO {

    @NotBlank(message = "评论不能为空")
    @Size(min=2,max=1000,message = "最少2个字符，不多于1000字")
    private String content;

}
