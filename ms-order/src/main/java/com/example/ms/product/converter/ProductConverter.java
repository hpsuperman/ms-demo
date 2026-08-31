package com.example.ms.product.converter;

import com.example.ms.product.dto.ProductRequest;
import com.example.ms.product.dto.ProductResponse;
import com.example.ms.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface ProductConverter {
    Product toEntity(ProductRequest request);

    ProductResponse toResponse(Product product);

    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "createdAt")
    @Mapping(ignore = true, target = "updatedAt")
    @Mapping(ignore = true, target = "deletedAt")
    void updateEntity(ProductRequest request, @MappingTarget Product product);
}
