package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderItemRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderItemResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderItemService {
    OrderItemResponseDto createOrderItem(Long orderShopId, OrderItemRequestDto requestDto);
    OrderItemResponseDto getOrderItemById(Long id);
    Page<OrderItemResponseDto> getOrderItemsByOrderShopId(Long orderShopId, Pageable pageable);
    Page<OrderItemResponseDto> getOrderItemsByProductVariantId(Long productVariantId, Pageable pageable);
}
