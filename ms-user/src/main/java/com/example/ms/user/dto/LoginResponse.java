package com.example.ms.user.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private RegisterResponse user;
}
