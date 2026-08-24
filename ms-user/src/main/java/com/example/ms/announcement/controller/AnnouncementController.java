package com.example.ms.announcement.controller;

import com.example.ms.announcement.dto.AnnouncementRequest;
import com.example.ms.announcement.dto.AnnouncementResponse;
import com.example.ms.announcement.service.AnnouncementService;
import com.example.ms.common.ApiResponse;
import com.example.ms.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/announcement")
@RequiredArgsConstructor
@Tag(name = "公告", description = "公告增删改查")

public class AnnouncementController {
    private final AnnouncementService announcementService;

    @PostMapping
    @Operation(summary = "新增公告")
    public ApiResponse<AnnouncementResponse> create(@Valid @RequestBody AnnouncementRequest request) {
        return ApiResponse.success(announcementService.create(request));
    }

    @GetMapping("/page")
    @Operation(summary = "查询公告")
    public ApiResponse<PageResponse<AnnouncementResponse>> page(@RequestParam(required = false) String keyword, @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.success(announcementService.page(keyword, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询公告详情")
    public ApiResponse<AnnouncementResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(announcementService.detail(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除公告")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新公告")
    public ApiResponse<AnnouncementResponse> update(@PathVariable Long id, @Valid @RequestBody AnnouncementRequest request) {
        return ApiResponse.success(announcementService.update(id, request));
    }
}
