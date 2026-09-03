package com.example.ms.stock.purchase.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.ms.common.PageResponse;
import com.example.ms.common.util.SnowflakeIdUtil;
import com.example.ms.exception.BusinessException;
import com.example.ms.exception.ErrorCode;
import com.example.ms.stock.purchase.client.ProductClient;
import com.example.ms.stock.purchase.client.ProductDTO;
import com.example.ms.stock.purchase.converter.PurchaseConverter;
import com.example.ms.stock.purchase.dto.PurchaseOrderItemRequest;
import com.example.ms.stock.purchase.dto.PurchaseOrderItemResponse;
import com.example.ms.stock.purchase.dto.PurchaseOrderRequest;
import com.example.ms.stock.purchase.dto.PurchaseOrderResponse;
import com.example.ms.stock.purchase.entity.PurchaseOrder;
import com.example.ms.stock.purchase.entity.PurchaseOrderItem;
import com.example.ms.stock.purchase.enums.PurchaseOrderStatus;
import com.example.ms.stock.purchase.mapper.PurchaseOrderItemMapper;
import com.example.ms.stock.purchase.mapper.PurchaseOrderMapper;
import com.example.ms.stock.supplier.entity.Supplier;
import com.example.ms.stock.supplier.mapper.SupplierMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor

public class PurchaseService {
    private final PurchaseConverter purchaseConverter;
    private final SupplierMapper supplierMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final ProductClient productClient;

    @Transactional
    public PurchaseOrderResponse create(PurchaseOrderRequest request) {
        PurchaseOrder purchaseOrder = purchaseConverter.toEntity(request);
        purchaseOrder.setStatus(PurchaseOrderStatus.DRAFT);
        purchaseOrder.setOrderNo("PO" + SnowflakeIdUtil.nextId());

        Supplier supplier = supplierMapper.selectById(request.getSupplierId());
        if (supplier == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到该供应商信息");
        }
        purchaseOrder.setSupplierName(supplier.getName());
        List<PurchaseOrderItemRequest> items = request.getItems();
        Integer totalAmount = 0;
        List<PurchaseOrderItem> orderItems = new ArrayList<>();
        for (PurchaseOrderItemRequest item : items) {
            ProductDTO productDTO = productClient.detail(item.getProductId()).getData();
            if (productDTO == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到该商品信息");
            }
            PurchaseOrderItem orderItem = new PurchaseOrderItem();
            orderItem.setProductId(productDTO.getId());
            orderItem.setProductName(productDTO.getName());
            orderItem.setPrice(item.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setAmount(item.getPrice() * item.getQuantity());
            totalAmount += orderItem.getAmount();
            orderItems.add(orderItem);
        }

        purchaseOrder.setTotalAmount(totalAmount);
        purchaseOrderMapper.insert(purchaseOrder);

        for (PurchaseOrderItem orderItem : orderItems) {
            orderItem.setOrderId(purchaseOrder.getId());
            purchaseOrderItemMapper.insert(orderItem);
        }

        return purchaseConverter.toResponse(purchaseOrder);
    }

    @Transactional(readOnly = true)
    public PageResponse<PurchaseOrderResponse> page(PurchaseOrderStatus status, Pageable pageable) {
        IPage<PurchaseOrder> mpPage = PageResponse.toMpPage(pageable);
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<PurchaseOrder>().eq(status != null, PurchaseOrder::getStatus, status).orderByDesc(PurchaseOrder::getCreatedAt);
        IPage<PurchaseOrder> page = purchaseOrderMapper.selectPage(mpPage, wrapper);
        return PageResponse.from(page, purchaseConverter::toResponse);
    }

    @Transactional
    public PurchaseOrderResponse update(Long id, PurchaseOrderRequest request) {
        PurchaseOrder purchaseOrder = purchaseOrderMapper.selectById(id);
        if (purchaseOrder == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到该采购订单信息");
        }
        if (!purchaseOrder.getStatus().equals(PurchaseOrderStatus.DRAFT)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前状态无法更改");
        }
        Supplier supplier = supplierMapper.selectById(request.getSupplierId());
        if (supplier == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到该供应商信息");
        }
        purchaseOrder.setSupplierId(supplier.getId());
        purchaseOrder.setSupplierName(supplier.getName());

        purchaseConverter.updateEntity(request, purchaseOrder);

        List<PurchaseOrderItemRequest> items = request.getItems();
        Integer totalAmount = 0;
        List<PurchaseOrderItem> orderItems = new ArrayList<>();

        purchaseOrderItemMapper.delete(new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getOrderId, id));
        for (PurchaseOrderItemRequest item : items) {
            ProductDTO productDTO = productClient.detail(item.getProductId()).getData();
            if (productDTO == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到该商品信息");
            }
            PurchaseOrderItem orderItem = new PurchaseOrderItem();
            orderItem.setOrderId(id);
            orderItem.setProductId(productDTO.getId());
            orderItem.setProductName(productDTO.getName());
            orderItem.setPrice(item.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setAmount(item.getPrice() * item.getQuantity());
            totalAmount += orderItem.getAmount();
            purchaseOrderItemMapper.insert(orderItem);
            orderItems.add(orderItem);
        }

        purchaseOrder.setTotalAmount(totalAmount);
        purchaseOrderMapper.updateById(purchaseOrder);
        return purchaseConverter.toResponse(purchaseOrder);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse detail(Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderMapper.selectById(id);
        if (purchaseOrder == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到该采购订单信息");
        }
        LambdaQueryWrapper<PurchaseOrderItem> wrapper = new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getOrderId, purchaseOrder.getId());
        List<PurchaseOrderItem> purchaseOrderItems = purchaseOrderItemMapper.selectList(wrapper);
        PurchaseOrderResponse resp = purchaseConverter.toResponse(purchaseOrder);

        resp.setItems(purchaseOrderItems.stream().map(purchaseConverter::toResponse).toList());
        return resp;
    }

    @Transactional
    public void submit(Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderMapper.selectById(id);
        if (purchaseOrder == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到该采购订单信息");
        }
        if (!purchaseOrder.getStatus().equals(PurchaseOrderStatus.DRAFT)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前状态无法更改");
        }
        purchaseOrder.setStatus(PurchaseOrderStatus.SUBMITTED);
        purchaseOrderMapper.updateById(purchaseOrder);
    }

    @Transactional
    public void cancel(Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderMapper.selectById(id);
        if (purchaseOrder == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到该采购订单信息");
        }
        if (purchaseOrder.getStatus().equals(PurchaseOrderStatus.DRAFT) || purchaseOrder.getStatus().equals(PurchaseOrderStatus.SUBMITTED)) {
            purchaseOrder.setStatus(PurchaseOrderStatus.CANCELED);
            purchaseOrderMapper.updateById(purchaseOrder);
        } else {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前状态无法更改");
        }
    }

    @Transactional
    public void stock(Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderMapper.selectById(id);
        if (purchaseOrder == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到该采购订单信息");
        }
        if (!purchaseOrder.getStatus().equals(PurchaseOrderStatus.SUBMITTED)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前状态无法更改");
        }

        LambdaQueryWrapper<PurchaseOrderItem> wrapper = new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getOrderId, id);

        List<PurchaseOrderItem> itemResponses = purchaseOrderItemMapper.selectList(wrapper);
        for (PurchaseOrderItem item : itemResponses) {
            productClient.increaseStock(item.getProductId(), item.getQuantity());
        }
        purchaseOrder.setStatus(PurchaseOrderStatus.STOCKED);
        purchaseOrder.setStockedAt(LocalDateTime.now());

        purchaseOrderMapper.updateById(purchaseOrder);
    }
}