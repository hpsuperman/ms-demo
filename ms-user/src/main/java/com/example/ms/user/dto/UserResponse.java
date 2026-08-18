package com.example.ms.user.dto;

import com.example.ms.user.entity.UserStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String phone;
    private String nickname;
    private String employeeNo;
    private String gender;
    private LocalDate birthday;
    private Long departmentId;
    private String departmentName;
    private String position;
    private String email;
    private UserStatus status;
    private String roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
