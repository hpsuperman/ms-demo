package com.example.ms.stock.dto;

import com.example.ms.stock.enums.StockStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockResponse {
    private Long id;
    private String name;
    private String contactPerson;
    private String contactPhone;
    private String address;
    private StockStatus status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
