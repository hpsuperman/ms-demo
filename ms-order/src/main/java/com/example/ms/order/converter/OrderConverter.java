package com.example.ms.order.converter;

import com.example.ms.order.dto.OrderResponse;
import com.example.ms.order.entity.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderConverter {
    OrderResponse toResponse(Order order);
}