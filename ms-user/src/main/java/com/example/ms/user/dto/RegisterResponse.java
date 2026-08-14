package com.example.ms.user.dto;

import com.example.ms.user.entity.UserStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RegisterResponse {
    private Long id;
    private String phone;
    private String nickname;
    private String avatar;
    private String gender;
    private LocalDate birthday;
    private UserStatus status;
    private String roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
