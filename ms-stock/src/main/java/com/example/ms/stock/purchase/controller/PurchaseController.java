package com.example.ms.stock.purchase.controller;

import com.example.ms.common.ApiResponse;
import com.example.ms.common.PageResponse;
import com.example.ms.stock.purchase.dto.PurchaseOrderRequest;
import com.example.ms.stock.purchase.dto.PurchaseOrderResponse;
import com.example.ms.stock.purchase.enums.PurchaseOrderStatus;
import com.example.ms.stock.purchase.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stock/purchase")
@RequiredArgsConstructor
@Tag(name = "采购订单", description = "采购订单增删改查")
public class PurchaseController {
    private final PurchaseService purchaseService;

    @Operation(summary = "添加采购订单")
    @PostMapping
    public ApiResponse<PurchaseOrderResponse> create(@Valid @RequestBody PurchaseOrderRequest request) {
        return ApiResponse.success(purchaseService.create(request));
    }

    @Operation(summary = "查询采购订单")
    @GetMapping("/page")
    public ApiResponse<PageResponse<PurchaseOrderResponse>> page(
            @RequestParam(required = false) PurchaseOrderStatus status, @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.success(purchaseService.page(status, pageable));
    }

    @Operation(summary = "查询采购订单详情")
    @GetMapping("/detail/{id}")
    public ApiResponse<PurchaseOrderResponse> detail(
            @PathVariable Long id) {
        return ApiResponse.success(purchaseService.detail(id));
    }

    @Operation(summary = "更新采购订单")
    @PutMapping("/{id}")
    public ApiResponse<PurchaseOrderResponse> update(@PathVariable Long id, @Valid @RequestBody PurchaseOrderRequest request) {
        return ApiResponse.success(purchaseService.update(id, request));
    }

    @Operation(summary = "提交采购订单")
    @PutMapping("/submit/{id}")
    public ApiResponse<Void> submit(@PathVariable Long id) {
        purchaseService.submit(id);
        return ApiResponse.success();
    }

    @Operation(summary = "下单采购订单")
    @PutMapping("/stock/{id}")
    public ApiResponse<Void> stock(@PathVariable Long id) {
        purchaseService.stock(id);
        return ApiResponse.success();
    }

    @Operation(summary = "取消采购订单")
    @PutMapping("/cancel/{id}")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        purchaseService.cancel(id);
        return ApiResponse.success();
    }
}
