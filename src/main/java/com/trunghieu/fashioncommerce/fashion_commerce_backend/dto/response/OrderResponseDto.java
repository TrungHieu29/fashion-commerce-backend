package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus; // Import OrderStatus
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    private Long id; // Đã đổi tên từ orderId thành id
    private Long userId;
    private String userFullName;
    private BigDecimal totalPrice;
    private BigDecimal finalPrice;
    private OrderStatus status; // Đã thay đổi từ String sang OrderStatus
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
