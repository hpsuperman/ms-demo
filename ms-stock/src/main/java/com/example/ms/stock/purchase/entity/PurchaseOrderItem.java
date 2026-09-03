package com.example.ms.stock.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.ms.common.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@TableName("t_purchase_order_item")
public class PurchaseOrderItem extends BaseEntity {
    private Long orderId;
    private Long productId;
    private String productName;
    private Integer price;
    private Integer quantity;
    private Integer amount;

    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}