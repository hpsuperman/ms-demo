package com.example.ms.stock.converter;

import com.example.ms.stock.dto.StockRequest;
import com.example.ms.stock.dto.StockResponse;
import com.example.ms.stock.entity.Stock;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StockConverter {
    Stock toEntity(StockRequest request);

    StockResponse toResponse(Stock stock);

    void updateEntity(StockRequest request, @MappingTarget Stock stock);
}
