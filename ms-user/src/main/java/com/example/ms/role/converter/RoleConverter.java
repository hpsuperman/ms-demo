package com.example.ms.role.converter;


import com.example.ms.role.dto.RoleRequest;
import com.example.ms.role.dto.RoleResponse;
import com.example.ms.role.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoleConverter {
    RoleResponse toResponse(Role role);

    Role toEntity(RoleRequest request);

    void updateEntity(RoleRequest request, @MappingTarget Role role);
}
