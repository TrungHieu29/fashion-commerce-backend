package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Order; // Import Order entity
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderShop; // Import OrderShop entity
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List; // Import List

public interface OrderService {
    OrderResponseDto createOrder(OrderRequestDto requestDto);
    OrderResponseDto getOrderById(Long id);
    Page<OrderResponseDto> getOrdersByUserId(Long userId, List<OrderStatus> shopStatuses, Pageable pageable); // Thay đổi chữ ký
    void deleteOrder(Long orderId);
    void replenishStock(OrderShop orderShop);
    void deductStock(OrderShop orderShop);
}
