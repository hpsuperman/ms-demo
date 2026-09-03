package com.example.ms.stock.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ms.stock.purchase.entity.PurchaseOrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PurchaseOrderItemMapper extends BaseMapper<PurchaseOrderItem> {
}
