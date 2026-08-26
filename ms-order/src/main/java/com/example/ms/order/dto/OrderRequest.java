package com.example.ms.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequest {
    @NotBlank(message = "商品名字不能为空")
    private String productName;

    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "不能小于1")
    private Integer quantity;

    @NotNull(message = "金额不能为空")
    @Min(value = 0, message = "不能小于0")
    private Integer amount;
    private String remark;
}
