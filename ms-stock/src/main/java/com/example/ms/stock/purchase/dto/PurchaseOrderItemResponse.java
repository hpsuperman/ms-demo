package com.example.ms.stock.purchase.dto;

import lombok.Data;

@Data
public class PurchaseOrderItemResponse {
    private Long productId;
    private String productName;
    private Integer price;
    private Integer quantity;
    private Integer amount;
}
