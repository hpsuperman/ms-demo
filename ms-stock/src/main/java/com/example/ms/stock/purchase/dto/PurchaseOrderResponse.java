package com.example.ms.stock.purchase.dto;

import com.example.ms.stock.purchase.enums.PurchaseOrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseOrderResponse {
    private Long id;
    private String orderNo;
    private Long supplierId;
    private String supplierName;
    private Integer totalAmount;
    private PurchaseOrderStatus status;
    private String remark;
    private LocalDateTime stockedAt;
    private List<PurchaseOrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
