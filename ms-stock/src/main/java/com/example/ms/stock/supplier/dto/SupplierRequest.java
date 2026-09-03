package com.example.ms.stock.supplier.dto;

import com.example.ms.stock.supplier.enums.SupplierStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SupplierRequest {
    @NotBlank(message = "请输入供应商名称")
    private String name;
    @NotBlank(message = "请输入联系人")
    private String contactPerson;
    @NotBlank(message = "请输入联系电话")
    private String contactPhone;
    @NotNull(message = "请输入状态")
    private SupplierStatus status;
    private String address;
    private String remark;
}
