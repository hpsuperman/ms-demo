package com.example.ms.stock.supplier.dto;

import com.example.ms.stock.supplier.enums.SupplierStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierResponse {
    private Long id;
    private String name;
    private String contactPerson;
    private String contactPhone;
    private String address;
    private SupplierStatus status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
