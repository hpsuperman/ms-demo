package com.example.ms.stock.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ms.stock.purchase.entity.PurchaseOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {
}
