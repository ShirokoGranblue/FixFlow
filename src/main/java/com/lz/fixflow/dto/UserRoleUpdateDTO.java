package com.lz.fixflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRoleUpdateDTO {

    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "USER|STAFF|ADMIN",message = "角色不合法")
    private String role;
}
