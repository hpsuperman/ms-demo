package com.example.ms.role.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ms.exception.BusinessException;
import com.example.ms.exception.ErrorCode;
import com.example.ms.role.converter.RoleConverter;
import com.example.ms.role.dto.RoleRequest;
import com.example.ms.role.dto.RoleResponse;
import com.example.ms.role.entity.Role;
import com.example.ms.role.entity.UserRole;
import com.example.ms.role.mapper.RoleMapper;
import com.example.ms.role.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleMapper roleMapper;
    private final RoleConverter roleConverter;
    private final UserRoleMapper userRoleMapper;

    public List<RoleResponse> list() {
        return roleMapper.selectList(null)
                .stream()
                .map(roleConverter::toResponse)
                .toList();
    }

    public RoleResponse create(RoleRequest request) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (roleMapper.selectCount(wrapper.eq(Role::getName, request.getName())) > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "角色名称已存在");
        }
        Role role = roleConverter.toEntity(request);
        roleMapper.insert(role);
        return roleConverter.toResponse(role);
    }

    public RoleResponse detail(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "当前角色不存在");
        }
        return roleConverter.toResponse(role);
    }

    public RoleResponse update(Long id, RoleRequest request) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "当前角色不存在");
        }
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<Role>().eq(Role::getName, request.getName()).ne(Role::getId, id);
        if (roleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "改名重复了");
        }
        roleConverter.updateEntity(request, role);
        roleMapper.updateById(role);
        return roleConverter.toResponse(role);
    }

    public void delete(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "当前角色不存在");
        }
        Long count = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));
        if (count > 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "有用户使用该角色，无法删除");
        }
        roleMapper.deleteById(id);
    }
}
