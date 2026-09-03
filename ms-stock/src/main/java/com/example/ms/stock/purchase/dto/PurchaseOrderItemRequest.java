package com.example.ms.stock.purchase.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PurchaseOrderItemRequest {
    @NotNull(message = "商品不能为空")
    private Long productId;
    @NotNull(message = "单价不能为空")
    @Min(value = 1, message = "单价不能小于1")
    private Integer price;
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量不能小于1")
    private Integer quantity;
}