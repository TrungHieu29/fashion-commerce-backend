package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderItemRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderItemResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderItem;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderShop;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductVariant;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.OrderItemMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderItemRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderShopRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductVariantRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderShopRepository orderShopRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OrderItemMapper orderItemMapper;

    @Override
    @Transactional(readOnly = true)
    public OrderItemResponseDto getOrderItemById(Long id) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem not found with id: " + id));
        return orderItemMapper.toDto(orderItem);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderItemResponseDto> getOrderItemsByOrderShopId(Long orderShopId, Pageable pageable) {
        return orderItemRepository.findByOrderShopId(orderShopId, pageable)
                .map(orderItemMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderItemResponseDto> getOrderItemsByProductVariantId(Long productVariantId, Pageable pageable) {
        return orderItemRepository.findByProductVariantId(productVariantId, pageable)
                .map(orderItemMapper::toDto);
    }
}
