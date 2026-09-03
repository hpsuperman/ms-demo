package com.example.ms.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    @Valid
    @NotEmpty(message = "订单明细不能为空")
    private List<OrderItemRequest> items;

    private String remark;
}