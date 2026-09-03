package com.example.ms.stock.purchase.converter;

import com.example.ms.stock.purchase.dto.PurchaseOrderItemRequest;
import com.example.ms.stock.purchase.dto.PurchaseOrderItemResponse;
import com.example.ms.stock.purchase.dto.PurchaseOrderRequest;
import com.example.ms.stock.purchase.dto.PurchaseOrderResponse;
import com.example.ms.stock.purchase.entity.PurchaseOrder;
import com.example.ms.stock.purchase.entity.PurchaseOrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PurchaseConverter {
    PurchaseOrder toEntity(PurchaseOrderRequest request);

    PurchaseOrderResponse toResponse(PurchaseOrder purchaseOrder);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(PurchaseOrderRequest request, @MappingTarget PurchaseOrder purchaseOrder);

    PurchaseOrderItem toEntity(PurchaseOrderItemRequest request);

    PurchaseOrderItemResponse toResponse(PurchaseOrderItem purchaseOrderItem);
}