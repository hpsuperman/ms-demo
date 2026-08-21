package com.example.ms.approval.controller;

import com.example.ms.approval.dto.ApprovalRequest;
import com.example.ms.approval.dto.LeaveDetailResponse;
import com.example.ms.approval.dto.LeaveItemResponse;
import com.example.ms.approval.dto.LeaveRequest;
import com.example.ms.approval.enums.LeaveStatus;
import com.example.ms.approval.service.LeaveService;
import com.example.ms.common.ApiResponse;
import com.example.ms.common.PageResponse;
import com.example.ms.common.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/leave")
@RequiredArgsConstructor
@Tag(name = "请假", description = "请假增删改查")
public class LeaveController {
    private final LeaveService leaveService;

    @Operation(summary = "新增请假申请")
    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody LeaveRequest request) {
        return ApiResponse.success(leaveService.create(UserContext.getUserId(), request));
    }

    @Operation(summary = "请假审核")
    @PostMapping("/{id}/review")
    public ApiResponse<Long> review(@PathVariable Long id, @Valid @RequestBody ApprovalRequest request) {
        return ApiResponse.success(leaveService.review(UserContext.getUserId(), id, request));
    }

    @Operation(summary = "请假分页")
    @GetMapping("/page")
    public ApiResponse<PageResponse<LeaveItemResponse>> page(
            @RequestParam(required = false) LeaveStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.success(leaveService.page(UserContext.getUserId(), status, pageable));
    }

    @Operation(summary = "请假详情")
    @GetMapping("/detail/{id}")
    public ApiResponse<LeaveDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(leaveService.detail(UserContext.getUserId(), id));
    }

    @Operation(summary = "请假详情")
    @PutMapping("/cancel/{id}")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        leaveService.cancel(UserContext.getUserId(), id);
        return ApiResponse.success();
    }

    @Operation(summary = "待办分页")
    @GetMapping("/todo")
    public ApiResponse<PageResponse<LeaveItemResponse>> todoPage(
            @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.success(leaveService.todoPage(UserContext.getUserId(), pageable));
    }
}
