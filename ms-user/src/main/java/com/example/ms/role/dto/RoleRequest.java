package com.example.ms.role.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleRequest {
    @NotBlank(message = "角色标识不能为空")
    private String name;
    @NotBlank(message = "角色描述不能为空")
    private String description;
}
