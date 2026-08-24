package com.example.ms.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.ms.common.PageResponse;
import com.example.ms.department.entity.Department;
import com.example.ms.department.mapper.DepartmentMapper;
import com.example.ms.exception.BusinessException;
import com.example.ms.exception.ErrorCode;
import com.example.ms.role.entity.Role;
import com.example.ms.role.entity.UserRole;
import com.example.ms.role.mapper.RoleMapper;
import com.example.ms.role.mapper.UserRoleMapper;
import com.example.ms.user.converter.UserConverter;
import com.example.ms.user.dto.*;
import com.example.ms.user.entity.User;
import com.example.ms.user.entity.UserStatus;
import com.example.ms.user.mapper.UserMapper;
import com.example.ms.user.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserConverter userConverter;
    private final DepartmentMapper departmentMapper;
    private final CaptchaService captchaService;
    private final JwtUtil jwtUtil;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;


    @Transactional
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

        userMapper.insert(user);

        Role defaultRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getName, "USER"));
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(defaultRole.getId());
        userRoleMapper.insert(userRole);

        return userConverter.toDto(user);
    }

    @Transactional
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

        List<String> roles = getRoleNames(user.getId());
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), String.join(",", roles));
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(userConverter.toDto(user));
        return response;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> page(String keyword, Pageable pageable) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>().orderByDesc(User::getId);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(User::getNickname, keyword).or().like(User::getPhone, keyword).or().like(User::getEmployeeNo, keyword);
        }
        IPage<User> mpPage = userMapper.selectPage(PageResponse.toMpPage(pageable), wrapper);
        PageResponse<UserResponse> result = PageResponse.from(mpPage, userConverter::toResponse);
        result.getContent().forEach(this::fillDepartmentName);
        return result;
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        Long phoneCount = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
        if (phoneCount > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "手机号已被使用");
        }
        Long empCount = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmployeeNo, request.getEmployeeNo()));
        if (empCount > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "工号已被使用");
        }
        User user = userConverter.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode("123456"));
        user.setStatus(UserStatus.ACTIVE);

        userMapper.insert(user);

        Role defaultRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getName, "USER"));
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(defaultRole.getId());
        userRoleMapper.insert(userRole);

        return userConverter.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse detail(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在");
        }
        UserResponse response = userConverter.toResponse(user);
        fillDepartmentName(response);
        return response;

    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在");
        }
        Long phoneCount = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()).ne(User::getId, id));

        if (phoneCount > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "手机号已被他人使用");
        }
        Long empCount = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmployeeNo, request.getEmployeeNo()).ne(User::getId, id));
        if (empCount > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "工号已被他人使用");
        }

        userConverter.updateEntity(request, user);
        userMapper.updateById(user);
        return userConverter.toResponse(user);

    }

    @Transactional
    public void delete(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在");
        }
        userMapper.deleteById(user);
    }

    private void fillDepartmentName(UserResponse response) {
        if (response.getDepartmentId() != null) {
            Department department = departmentMapper.selectById(response.getDepartmentId());
            response.setDepartmentName(department != null ? department.getName() : null);
        }
    }

    private List<String> getRoleNames(Long userId) {
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return List.of();
        }
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();
        return roleMapper.selectBatchIds(roleIds).stream()
                .map(Role::getName)
                .toList();
    }

    public List<UserResponse> listByRole(String name) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<Role>().eq(Role::getName, name);
        Role role = roleMapper.selectOne(wrapper);
        if (role == null) {
            return List.of();
        }
        List<UserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, role.getId()));
        List<Long> userIds = userRoles.stream().map(UserRole::getUserId).toList();
        if (userIds.isEmpty()) {
            return List.of();
        }

        List<User> users = userMapper.selectByIds(userIds);
        return users.stream().map(userConverter::toResponse).toList();
    }
}

