package com.example.ms.stock.dto;

import com.example.ms.stock.enums.StockStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StockRequest {
    @NotBlank(message = "请输入供应商名称")
    private String name;
    @NotBlank(message = "请输入联系名称")
    private String contactPerson;
    @NotBlank(message = "请输入联系电话")
    private String contactPhone;
    @NotBlank(message = "请输入状态")
    private StockStatus status;
}
