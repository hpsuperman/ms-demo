package com.example.ms.cart.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.ms.cart.converter.CartConverter;
import com.example.ms.cart.dto.CartRequest;
import com.example.ms.cart.dto.CartResponse;
import com.example.ms.cart.entity.Cart;
import com.example.ms.cart.mapper.CartMapper;
import com.example.ms.common.PageResponse;
import com.example.ms.common.context.UserContext;
import com.example.ms.exception.BusinessException;
import com.example.ms.exception.ErrorCode;
import com.example.ms.product.entity.Product;
import com.example.ms.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

public class CartService {
    private final ProductMapper productMapper;
    private final CartMapper cartMapper;
    private final CartConverter cartConverter;

    @Transactional
    public CartResponse create(CartRequest request) {
        Long userId = UserContext.getUserId();

        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到当前商品");
        }
        Cart existing = cartMapper.selectOne(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId).eq(Cart::getProductId, request.getProductId()));
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            cartMapper.updateById(existing);
            return cartConverter.toResponse(existing);
        }
        Cart cart = cartConverter.toEntity(request);
        cart.setUserId(userId);
        cartMapper.insert(cart);
        return cartConverter.toResponse(cart);
    }

    @Transactional(readOnly = true)
    public PageResponse<CartResponse> page(Pageable pageable) {
        IPage<Cart> mpPage = PageResponse.toMpPage(pageable);
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<Cart>().orderByDesc(Cart::getCreatedAt);
        IPage<Cart> page = cartMapper.selectPage(mpPage, wrapper);
        return PageResponse.from(page, cart -> {
            Product product = productMapper.selectById(cart.getProductId());
            CartResponse resp = cartConverter.toResponse(cart);
            if (product != null) {
                resp.setProductName(product.getName());
                resp.setProductPrice(product.getPrice());
                resp.setSubtotal(product.getPrice() * cart.getQuantity());
            }
            return resp;
        });
    }

    @Transactional
    public CartResponse update(Long id, Integer quantity) {
        Cart cart = cartMapper.selectOne(new LambdaQueryWrapper<Cart>().eq(Cart::getId, id).eq(Cart::getUserId, UserContext.getUserId()));
        if (cart == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到当前购物车商品");
        }
        if (quantity < 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前数量不能小于1");
        }
        cart.setQuantity(quantity);
        cartMapper.updateById(cart);
        return cartConverter.toResponse(cart);
    }

    @Transactional
    public void delete(Long id) {
        Cart cart = cartMapper.selectOne(new LambdaQueryWrapper<Cart>().eq(Cart::getId, id).eq(Cart::getUserId, UserContext.getUserId()));
        if (cart == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "查询不到当前购物车商品");
        }
        cartMapper.deleteById(cart);
    }

}
