package com.example.ms.stock.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.ms.common.BaseEntity;
import com.example.ms.stock.purchase.enums.PurchaseOrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@TableName("t_purchase_order")
public class PurchaseOrder extends BaseEntity {
    private String orderNo;
    private Long supplierId;
    private String supplierName;
    private Integer totalAmount;
    private PurchaseOrderStatus status;
    private String remark;
    private LocalDateTime stockedAt;

    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
