package com.example.ms.user.converter;

import com.example.ms.user.dto.RegisterResponse;
import com.example.ms.user.dto.UserRequest;
import com.example.ms.user.dto.UserResponse;
import com.example.ms.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserConverter {
    RegisterResponse toDto(User user);

    UserResponse toResponse(User user);

    User toEntity(UserRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UserRequest request, @MappingTarget User user);
}