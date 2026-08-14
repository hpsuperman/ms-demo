package com.example.ms.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ms.exception.BusinessException;
import com.example.ms.exception.ErrorCode;
import com.example.ms.user.converter.UserConverter;
import com.example.ms.user.dto.LoginRequest;
import com.example.ms.user.dto.LoginResponse;
import com.example.ms.user.dto.RegisterRequest;
import com.example.ms.user.dto.RegisterResponse;
import com.example.ms.user.entity.User;
import com.example.ms.user.entity.UserRole;
import com.example.ms.user.entity.UserStatus;
import com.example.ms.user.mapper.UserMapper;
import com.example.ms.user.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserConverter userConverter;
    private final CaptchaService captchaService;
    private final JwtUtil jwtUtil;

    public RegisterResponse register(RegisterRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone());
        User existing = userMapper.selectOne(wrapper);
        if (existing != null) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "当前手机号已被注册");
        }


        User user = new User();
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(String.valueOf(UserRole.USER));

        userMapper.insert(user);
        return userConverter.toDto(user);
    }

    public LoginResponse login(LoginRequest request) {
        captchaService.verifyCaptcha(request.getCaptchaId(), request.getCaptcha());

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
        if (user == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "密码错误");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getRoles());
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(userConverter.toDto(user));
        return response;
    }
}

