package com.example.ms.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentRequest {
    private Long parentId;

    @NotBlank(message = "名字不能为空")
    @Size(max = 50)
    private String name;

    private Long leaderId;
    private Integer sort;
}
