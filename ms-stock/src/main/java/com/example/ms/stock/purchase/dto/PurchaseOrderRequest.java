package com.example.ms.stock.purchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PurchaseOrderRequest {
    @NotNull(message = "供应商不能为空")
    private Long supplierId;
    private String remark;
    @Valid
    @NotEmpty(message = "采购明细不能为空")
    private List<PurchaseOrderItemRequest> items;

}