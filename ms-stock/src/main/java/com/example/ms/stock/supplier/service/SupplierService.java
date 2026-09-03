package com.example.ms.stock.supplier.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.ms.common.PageResponse;
import com.example.ms.exception.BusinessException;
import com.example.ms.exception.ErrorCode;
import com.example.ms.stock.supplier.converter.SupplierConverter;
import com.example.ms.stock.supplier.dto.SupplierRequest;
import com.example.ms.stock.supplier.dto.SupplierResponse;
import com.example.ms.stock.supplier.entity.Supplier;
import com.example.ms.stock.supplier.enums.SupplierStatus;
import com.example.ms.stock.supplier.mapper.SupplierMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupplierService {
    private final SupplierConverter supplierConverter;
    private final SupplierMapper supplierMapper;

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        Supplier supplier = supplierConverter.toEntity(request);
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<Supplier>().eq(Supplier::getName, request.getName());
        Long count = supplierMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "已存在该供应商名称，请重新填写其他供应商名称");
        }
        supplierMapper.insert(supplier);
        return supplierConverter.toResponse(supplier);
    }

    @Transactional(readOnly = true)
    public PageResponse<SupplierResponse> page(SupplierStatus status, Pageable pageable) {
        IPage<Supplier> mpPage = PageResponse.toMpPage(pageable);
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<Supplier>().eq(status != null, Supplier::getStatus, status).orderByDesc(Supplier::getCreatedAt);
        IPage<Supplier> page = supplierMapper.selectPage(mpPage, wrapper);
        return PageResponse.from(page, supplierConverter::toResponse);
    }

    @Transactional(readOnly = true)
    public SupplierResponse detail(Long id) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到该供应商数据");
        }
        return supplierConverter.toResponse(supplier);
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = supplierMapper.selectById(id);

        if (supplier == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到该供应商数据");
        }

        Long count = supplierMapper.selectCount(new LambdaQueryWrapper<Supplier>().eq(Supplier::getName, request.getName()).ne(Supplier::getId, id));
        if (count > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "存在同名供应商，请填写其他名称");
        }
        supplierConverter.updateEntity(request, supplier);
        supplierMapper.updateById(supplier);
        return supplierConverter.toResponse(supplier);
    }

    @Transactional
    public void delete(Long id) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到该供应商数据");
        }
        supplierMapper.deleteById(supplier);
    }

    @Transactional
    public SupplierResponse updateStatus(Long id, SupplierStatus status) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到该供应商数据");
        }
        supplier.setStatus(status);
        supplierMapper.updateById(supplier);
        return supplierConverter.toResponse(supplier);
    }
}
