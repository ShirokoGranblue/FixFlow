package com.lz.fixflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserLoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 1,max = 50,message = "用户名长度为1-50个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8,max = 16,message = "密码长度为8-16位")
    private String password;
}
