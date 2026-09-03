package com.example.ms.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.ms.common.PageResponse;
import com.example.ms.common.context.UserContext;
import com.example.ms.common.util.RedisUtil;
import com.example.ms.common.util.SnowflakeIdUtil;
import com.example.ms.exception.BusinessException;
import com.example.ms.exception.ErrorCode;
import com.example.ms.order.converter.OrderConverter;
import com.example.ms.order.converter.OrderItemConverter;
import com.example.ms.order.dto.OrderItemRequest;
import com.example.ms.order.dto.OrderItemResponse;
import com.example.ms.order.dto.OrderRequest;
import com.example.ms.order.dto.OrderResponse;
import com.example.ms.order.entity.Order;
import com.example.ms.order.entity.OrderItem;
import com.example.ms.order.enums.OrderStatus;
import com.example.ms.order.mapper.OrderItemMapper;
import com.example.ms.order.mapper.OrderMapper;
import com.example.ms.product.entity.Product;
import com.example.ms.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderConverter orderConverter;
    private final OrderItemConverter orderItemConverter;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final RedisUtil redisUtil;

    @Transactional
    public OrderResponse create(OrderRequest request) {
        int totalAmount = 0;
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest item : request.getItems()) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商品不存在");
            }
            int affected = productMapper.deductStock(product.getId(), item.getQuantity());
            if (affected == 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "商品库存不足：" + product.getName());
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setAmount(product.getPrice() * item.getQuantity());
            totalAmount += orderItem.getAmount();
            orderItems.add(orderItem);
        }

        Order order = new Order();
        order.setOrderNo("OD" + SnowflakeIdUtil.nextId());
        order.setUserId(UserContext.getUserId());
        order.setAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        order.setRemark(request.getRemark());
        orderMapper.insert(order);

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
            orderItemMapper.insert(orderItem);
        }
        return orderConverter.toResponse(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> page(OrderStatus status, Pageable pageable) {
        IPage<Order> mpPage = PageResponse.toMpPage(pageable);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>().eq(status != null, Order::getStatus, status).orderByDesc(Order::getCreatedAt);
        IPage<Order> page = orderMapper.selectPage(mpPage, wrapper);
        return PageResponse.from(page, orderConverter::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse detail(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "订单不存在");
        }
        OrderResponse response = orderConverter.toResponse(order);
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        List<OrderItemResponse> itemResponses = items.stream().map(orderItemConverter::toResponse).toList();
        response.setItems(itemResponses);
        return response;
    }

    @Transactional
    public void delete(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "订单不存在");
        }
        orderMapper.deleteById(order);
    }

    @Transactional
    public OrderResponse pay(Long id) {
        String lockKey = "lock:order:" + id;
        if (!redisUtil.tryLock(lockKey, 30, TimeUnit.SECONDS)) {
            throw new BusinessException(ErrorCode.RATE_LIMITED, "订单操作进行中，请稍后再试");
        }
        try {
            Order order = orderMapper.selectById(id);
            if (order == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "订单不存在");
            }
            if (!order.getStatus().equals(OrderStatus.PENDING)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "当前状态不能支付");
            }
            order.setPaidAt(LocalDateTime.now());
            order.setStatus(OrderStatus.PAID);
            orderMapper.updateById(order);
            return orderConverter.toResponse(order);
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

    @Transactional
    public OrderResponse cancel(Long id) {
        String lockKey = "lock:order:" + id;
        if (!redisUtil.tryLock(lockKey, 30, TimeUnit.SECONDS)) {
            throw new BusinessException(ErrorCode.RATE_LIMITED, "订单操作进行中，请稍后再试");
        }
        try {
            Order order = orderMapper.selectById(id);
            if (order == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "订单不存在");
            }
            if (!order.getStatus().equals(OrderStatus.PENDING)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "当前状态不能取消");
            }
            order.setCanceledAt(LocalDateTime.now());
            order.setStatus(OrderStatus.CANCELED);
            orderMapper.updateById(order);
            return orderConverter.toResponse(order);
        } finally {
            redisUtil.unlock(lockKey);
        }
    }

}