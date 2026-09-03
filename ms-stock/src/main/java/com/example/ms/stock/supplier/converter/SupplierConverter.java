package com.example.ms.stock.supplier.converter;

import com.example.ms.stock.supplier.dto.SupplierRequest;
import com.example.ms.stock.supplier.dto.SupplierResponse;
import com.example.ms.stock.supplier.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SupplierConverter {
    Supplier toEntity(SupplierRequest request);

    SupplierResponse toResponse(Supplier stock);

    @Mapping(target = "id",ignore = true)
    @Mapping(target = "createdAt",ignore = true)
    @Mapping(target = "updatedAt",ignore = true)
    @Mapping(target = "deletedAt",ignore = true)
    void updateEntity(SupplierRequest request, @MappingTarget Supplier stock);
}
