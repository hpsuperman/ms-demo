package com.example.ms.order.converter;

import com.example.ms.order.dto.OrderItemResponse;
import com.example.ms.order.entity.OrderItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemConverter {
    OrderItemResponse toResponse(OrderItem orderItem);
}