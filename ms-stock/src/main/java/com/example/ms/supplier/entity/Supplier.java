package com.example.ms.stock.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.ms.common.BaseEntity;
import com.example.ms.stock.enums.StockStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@TableName("t_supplier")
public class Stock extends BaseEntity {
    private String name;
    private String contactPerson;
    private String contactPhone;
    private String address;
    private StockStatus status;
    private String remark;

    @TableLogic(value = "null", delval = "now()")
    private String deletedAt;
}
