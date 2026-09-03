package com.example.ms.order.dto;

import lombok.Data;

@Data
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Integer price;
    private Integer quantity;
    private Integer amount;
}