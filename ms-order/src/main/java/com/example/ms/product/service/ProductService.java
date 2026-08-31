package com.example.ms.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.ms.common.PageResponse;
import com.example.ms.exception.BusinessException;
import com.example.ms.exception.ErrorCode;
import com.example.ms.product.converter.ProductConverter;
import com.example.ms.product.dto.ProductRequest;
import com.example.ms.product.dto.ProductResponse;
import com.example.ms.product.entity.Product;
import com.example.ms.product.enums.ProductStatus;
import com.example.ms.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

public class ProductService {
    private final ProductConverter productConverter;
    private final ProductMapper productMapper;

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = productConverter.toEntity(request);
        productMapper.insert(product);
        return productConverter.toResponse(product);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> page(ProductStatus status, Pageable pageable) {
        IPage<Product> mpPage = PageResponse.toMpPage(pageable);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>().eq(status != null, Product::getStatus, status).orderByDesc(Product::getCreatedAt);
        IPage<Product> page = productMapper.selectPage(mpPage, wrapper);
        return PageResponse.from(page, productConverter::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse detail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到当前商品");
        }
        return productConverter.toResponse(product);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到当前商品");
        }
        productConverter.updateEntity(request, product);
        productMapper.updateById(product);
        return productConverter.toResponse(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到当前商品");
        }
        productMapper.deleteById(product);
    }
}
