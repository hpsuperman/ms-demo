package com.example.ms.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.ms.common.PageResponse;
import com.example.ms.common.context.UserContext;
import com.example.ms.common.util.SnowflakeIdUtil;
import com.example.ms.exception.BusinessException;
import com.example.ms.exception.ErrorCode;
import com.example.ms.order.converter.OrderConverter;
import com.example.ms.order.dto.OrderRequest;
import com.example.ms.order.dto.OrderResponse;
import com.example.ms.order.entity.Order;
import com.example.ms.order.enums.OrderStatus;
import com.example.ms.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderConverter orderConverter;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse create(OrderRequest request) {
        Order order = orderConverter.toEntity(request);
        order.setUserId(UserContext.getUserId());
        order.setOrderNo("OD" + SnowflakeIdUtil.nextId());
        order.setStatus(OrderStatus.PENDING);
        orderMapper.insert(order);
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
        return orderConverter.toResponse(order);
    }

    @Transactional
    public OrderResponse update(Long id, OrderRequest request) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "订单不存在");
        }
        orderConverter.updateEntity(request, order);
        orderMapper.updateById(order);
        return orderConverter.toResponse(order);
    }

    @Transactional
    public void delete(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "订单不存在");
        }
        orderMapper.deleteById(order);
    }
}
