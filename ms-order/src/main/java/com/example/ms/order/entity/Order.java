package com.example.ms.order.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.ms.common.BaseEntity;
import com.example.ms.order.enums.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@TableName("t_order")
public class Order extends BaseEntity {
    private String orderNo;
    private Long userId;
    private String productName;
    private Integer quantity;
    private Integer amount;
    private OrderStatus status;
    private String remark;
    private LocalDateTime paidAt;
    private LocalDateTime canceledAt;
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
