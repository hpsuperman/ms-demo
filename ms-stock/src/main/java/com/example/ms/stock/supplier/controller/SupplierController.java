package com.example.ms.stock.supplier.controller;

import com.example.ms.common.ApiResponse;
import com.example.ms.common.PageResponse;
import com.example.ms.stock.supplier.dto.SupplierRequest;
import com.example.ms.stock.supplier.dto.SupplierResponse;
import com.example.ms.stock.supplier.enums.SupplierStatus;
import com.example.ms.stock.supplier.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stock/supplier")
@RequiredArgsConstructor
@Tag(name = "供应商", description = "供应商增删改查")
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "添加供应商")
    @PostMapping
    public ApiResponse<SupplierResponse> create(@Valid @RequestBody SupplierRequest request) {
        return ApiResponse.success(supplierService.create(request));
    }

    @Operation(summary = "查询供应商列表")
    @GetMapping("/page")
    public ApiResponse<PageResponse<SupplierResponse>> page(@RequestParam(required = false) SupplierStatus status, @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.success(supplierService.page(status, pageable));
    }

    @Operation(summary = "查询供应商详情")
    @GetMapping("/{id}")
    public ApiResponse<SupplierResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(supplierService.detail(id));
    }

    @Operation(summary = "更新供应商")
    @PutMapping("/{id}")
    public ApiResponse<SupplierResponse> update(@PathVariable Long id,@Valid @RequestBody SupplierRequest request) {
        return ApiResponse.success(supplierService.update(id, request));
    }

    @Operation(summary = "删除供应商")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ApiResponse.success();
    }

    @Operation(summary = "更新供应商状态")
    @PutMapping("/{id}/status")
    public ApiResponse<SupplierResponse> updateStatus(@PathVariable Long id, SupplierStatus status) {
        return ApiResponse.success(supplierService.updateStatus(id, status));
    }
}
