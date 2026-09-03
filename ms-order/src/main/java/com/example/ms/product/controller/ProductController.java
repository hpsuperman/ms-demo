package com.example.ms.product.controller;

import com.example.ms.common.ApiResponse;
import com.example.ms.common.PageResponse;
import com.example.ms.product.dto.ProductRequest;
import com.example.ms.product.dto.ProductResponse;
import com.example.ms.product.enums.ProductStatus;
import com.example.ms.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order/product")
@RequiredArgsConstructor
@Tag(name = "商品", description = "商品增删改查")
public class ProductController {
    private final ProductService productService;

    @Operation(summary = "新增商品")
    @PostMapping
    public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ApiResponse.success(productService.create(request));
    }

    @Operation(summary = "查询商品")
    @GetMapping("/page")
    public ApiResponse<PageResponse<ProductResponse>> page(@RequestParam(required = false) ProductStatus status, @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.success(productService.page(status, pageable));
    }

    @Operation(summary = "查询商品详情")
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(productService.detail(id));
    }

    @Operation(summary = "更新商品")
    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ApiResponse.success(productService.update(id, request));
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.success();
    }

    @Operation(summary = "增加库存")
    @PutMapping("/{id}/stock/increase")
    public ApiResponse<Void> increaseStock(@PathVariable Long id, @RequestParam Integer quantity) {
        productService.increaseStock(id, quantity);
        return ApiResponse.success();
    }
}
