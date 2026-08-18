package com.example.ms.user.controller;

import com.example.ms.common.ApiResponse;
import com.example.ms.common.PageResponse;
import com.example.ms.common.context.UserContext;
import com.example.ms.user.config.RequireRole;
import com.example.ms.user.dto.*;
import com.example.ms.user.service.CaptchaService;
import com.example.ms.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户", description = "用户增删改查")
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

    @Operation(summary = "获取当前用户")
    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.success(userService.detail(UserContext.getUserId()));
    }

    @Operation(summary = "查询列表")
    @GetMapping("/page")
    public ApiResponse<PageResponse<UserResponse>> page(@RequestParam(required = false) String keyword, @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.success(userService.page(keyword, pageable));
    }

    @RequireRole({"ADMIN"})
    @Operation(summary = "创建用户")
    @PostMapping
    public ApiResponse<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ApiResponse.success(userService.create(request));
    }

    @Operation(summary = "查询用户")
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(userService.detail(id));
    }

    @RequireRole({"ADMIN"})
    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return ApiResponse.success(userService.update(id, request));
    }

    @RequireRole({"ADMIN"})
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.success();
    }
}
