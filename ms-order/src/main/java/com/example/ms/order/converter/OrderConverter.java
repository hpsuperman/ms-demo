package com.example.ms.order.converter;

import com.example.ms.order.dto.OrderRequest;
import com.example.ms.order.dto.OrderResponse;
import com.example.ms.order.entity.Order;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OrderConverter {
    Order toEntity(OrderRequest request);

    OrderResponse toResponse(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(OrderRequest request, @MappingTarget Order order);
}
