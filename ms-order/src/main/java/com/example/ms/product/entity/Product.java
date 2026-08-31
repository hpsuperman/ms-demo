package com.example.ms.product.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.ms.common.BaseEntity;
import com.example.ms.product.enums.ProductStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@TableName("t_product")
public class Product extends BaseEntity {
    private String name;
    private Integer price;
    private Integer stock;
    private ProductStatus status;

    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}
