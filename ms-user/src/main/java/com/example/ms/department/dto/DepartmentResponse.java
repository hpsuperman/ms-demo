package com.example.ms.department.dto;

import com.example.ms.department.entity.DepartmentStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DepartmentResponse {
    private Long id;
    private Long parentId;
    private String name;
    private Long leaderId;
    private Integer sort;

    private String leaderName;
    private Integer memberCount;
    private List<DepartmentResponse> children;

    private DepartmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
