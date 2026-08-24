package com.example.ms.role.controller;

import com.example.ms.common.ApiResponse;
import com.example.ms.role.dto.RoleRequest;
import com.example.ms.role.dto.RoleResponse;
import com.example.ms.role.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/role")
@RequiredArgsConstructor
@Tag(name = "角色", description = "角色增删改查")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "查询列表")
    @GetMapping
    public ApiResponse<List<RoleResponse>> list() {
        return ApiResponse.success(roleService.list());
    }

    @Operation(summary = "新增角色")
    @PostMapping
    public ApiResponse<RoleResponse> create(@Valid @RequestBody RoleRequest request) {
        return ApiResponse.success(roleService.create(request));
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public ApiResponse<RoleResponse> update(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return ApiResponse.success(roleService.update(id, request));
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResponse.success();
    }

    @Operation(summary = "查询角色详情")
    @GetMapping("/detail/{id}")
    public ApiResponse<RoleResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(roleService.detail(id));
    }
}
