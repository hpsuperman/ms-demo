package com.example.ms.user.controller;

import com.example.ms.common.ApiResponse;
import com.example.ms.user.dto.CaptchaResponse;
import com.example.ms.user.dto.LoginRequest;
import com.example.ms.user.dto.LoginResponse;
import com.example.ms.user.dto.RegisterRequest;
import com.example.ms.user.dto.RegisterResponse;
import com.example.ms.user.service.CaptchaService;
import com.example.ms.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户", description = "用户注册 / 登录")
public class UserController {

    private final UserService userService;
    private final CaptchaService captchaService;

    @Operation(summary = "获取登录验证码")
    @GetMapping("/captcha")
    public ApiResponse<CaptchaResponse> captcha() {
        return ApiResponse.success(captchaService.generateCaptcha());
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(userService.register(request));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }
}
