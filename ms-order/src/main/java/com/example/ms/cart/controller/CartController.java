package com.example.ms.cart.controller;

import com.example.ms.cart.dto.CartRequest;
import com.example.ms.cart.dto.CartResponse;
import com.example.ms.cart.service.CartService;
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
@RequestMapping("/order/cart")
@RequiredArgsConstructor
@Tag(name = "购物车", description = "购物车增删改查")
public class CartController {
    private final CartService cartService;

    @Operation(summary = "添加购物车")
    @PostMapping
    public ApiResponse<CartResponse> create(@Valid @RequestBody CartRequest request) {
        return ApiResponse.success(cartService.create(request));
    }

    @Operation(summary = "查询购物车")
    @GetMapping
    public ApiResponse<PageResponse<CartResponse>> page(@PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.success(cartService.page(pageable));
    }

    @Operation(summary = "更新购物车")
    @PutMapping("/{id}")
    public ApiResponse<CartResponse> update(@PathVariable Long id, @RequestParam Integer quantity) {
        return ApiResponse.success(cartService.update(id, quantity));
    }

    @Operation(summary = "删除购物车")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        cartService.delete(id);
        return ApiResponse.success();
    }
}
