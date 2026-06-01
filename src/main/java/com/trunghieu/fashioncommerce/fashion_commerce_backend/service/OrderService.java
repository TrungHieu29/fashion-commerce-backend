package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Order; // Import Order entity
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderShop; // Import OrderShop entity
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponseDto createOrder(OrderRequestDto requestDto);
    OrderResponseDto getOrderById(Long id);
    Page<OrderResponseDto> getOrdersByUserId(Long userId, Pageable pageable);
    void deleteOrder(Long orderId);
    void replenishStock(OrderShop orderShop);
    void deductStock(OrderShop orderShop); // Thêm khai báo phương thức deductStock
}
