package com.example.ms.order.controller;

import com.example.ms.common.ApiResponse;
import com.example.ms.common.PageResponse;
import com.example.ms.order.dto.OrderRequest;
import com.example.ms.order.dto.OrderResponse;
import com.example.ms.order.enums.OrderStatus;
import com.example.ms.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Tag(name = "订单", description = "订单增删改查")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "新增订单")
    @PostMapping
    public ApiResponse<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        return ApiResponse.success(orderService.create(request));
    }

    @Operation(summary = "查询订单")
    @GetMapping("/page")
    public ApiResponse<PageResponse<OrderResponse>> page(@RequestParam(required = false) OrderStatus status, @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.success(orderService.page(status, pageable));
    }

    @Operation(summary = "查询订单详情")
    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(orderService.detail(id));
    }

    @Operation(summary = "更新订单")
    @PutMapping("/{id}")
    public ApiResponse<OrderResponse> update(@PathVariable Long id, @Valid @RequestBody OrderRequest request) {
        return ApiResponse.success(orderService.update(id, request));
    }

    @Operation(summary = "删除订单")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ApiResponse.success();
    }
}
