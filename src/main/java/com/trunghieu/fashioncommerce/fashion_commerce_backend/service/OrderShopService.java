package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderShopRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderShopResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus; // Import OrderStatus
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderShopService {
    OrderShopResponseDto getOrderShopById(Long id);

    Page<OrderShopResponseDto> getOrderShopsByOrderId(Long orderId, Pageable pageable);

    Page<OrderShopResponseDto> getOrderShopsByShopId(Long shopId, OrderStatus status, Pageable pageable); // Thay đổi chữ ký

    OrderShopResponseDto confirmDelivery(Long orderShopId);
    OrderShopResponseDto requestReturn(Long orderShopId);
    OrderShopResponseDto cancelOrderShop(Long orderShopId);
    OrderShopResponseDto confirmOrder(Long orderShopId);
}
