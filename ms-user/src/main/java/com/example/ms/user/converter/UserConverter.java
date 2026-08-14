package com.example.ms.user.converter;

import com.example.ms.user.dto.RegisterResponse;
import com.example.ms.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserConverter {
    RegisterResponse toDto(User user);
}