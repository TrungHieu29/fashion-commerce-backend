package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus; // Import OrderStatus
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set; // Import Set

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
    // private OrderStatus status; // Xóa trường status
    private String addressSnapshot;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<OrderShopResponseDto> orderShops; // Thêm trường orderShops
    private PaymentResponseDto payment; // Thêm trường payment
}
