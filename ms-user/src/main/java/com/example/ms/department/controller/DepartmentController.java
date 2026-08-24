package com.example.ms.department.controller;

import com.example.ms.common.ApiResponse;
import com.example.ms.department.dto.DepartmentRequest;
import com.example.ms.department.dto.DepartmentResponse;
import com.example.ms.department.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/department")
@RequiredArgsConstructor
@Tag(name = "部门", description = "部门增删改查")
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "查询列表")
    @GetMapping
    public ApiResponse<List<DepartmentResponse>> list() {
        return ApiResponse.success(departmentService.list());
    }

    @Operation(summary = "新增部门")
    @PostMapping
    public ApiResponse<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request) {
        return ApiResponse.success(departmentService.create(request));
    }

    @Operation(summary = "更新部门")
    @PutMapping("/{id}")
    public ApiResponse<DepartmentResponse> update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return ApiResponse.success(departmentService.update(id, request));
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ApiResponse.success();
    }

    @Operation(summary = "查询部门详情")
    @GetMapping("/detail/{id}")
    public ApiResponse<DepartmentResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(departmentService.detail(id));
    }
}
