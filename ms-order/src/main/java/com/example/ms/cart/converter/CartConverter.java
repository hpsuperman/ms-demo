package com.example.ms.cart.converter;

import com.example.ms.cart.dto.CartRequest;
import com.example.ms.cart.dto.CartResponse;
import com.example.ms.cart.entity.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CartConverter {
    Cart toEntity(CartRequest request);

    CartResponse toResponse(Cart cart);

    void updateEntity(CartRequest request, @MappingTarget Cart cart);
}
