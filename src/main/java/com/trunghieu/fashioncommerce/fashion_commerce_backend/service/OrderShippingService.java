package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderShippingRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderShippingResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderShippingService {
    OrderShippingResponseDto createOrderShipping(Long orderShopId, OrderShippingRequestDto requestDto);
    OrderShippingResponseDto getOrderShippingById(Long id);
    OrderShippingResponseDto getOrderShippingByOrderShopId(Long orderShopId);
    OrderShippingResponseDto updateOrderShipping(Long id, OrderShippingRequestDto requestDto);
}
