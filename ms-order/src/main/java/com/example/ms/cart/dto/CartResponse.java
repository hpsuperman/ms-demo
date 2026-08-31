package com.example.ms.cart.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data

public class CartResponse {
    private Long id;
    private Long productId;
    private Long userId;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String productName;
    private Integer productPrice;
    private Integer subtotal;
}
