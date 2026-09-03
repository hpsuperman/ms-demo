package com.example.ms.order.dto;

import com.example.ms.order.enums.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private String orderNo;
    private Long userId;
    private Integer amount;
    private String remark;
    private OrderStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime canceledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;
}