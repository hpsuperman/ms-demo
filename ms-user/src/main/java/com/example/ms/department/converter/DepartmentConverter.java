package com.example.ms.department.converter;

import com.example.ms.department.dto.DepartmentRequest;
import com.example.ms.department.dto.DepartmentResponse;
import com.example.ms.department.entity.Department;
import com.example.ms.department.entity.DepartmentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = DepartmentStatus.class)

public interface DepartmentConverter {
    DepartmentResponse toResponse(Department department);

    Department toEntity(DepartmentRequest request);

    void updateEntity(DepartmentRequest request, @MappingTarget Department department);
}
