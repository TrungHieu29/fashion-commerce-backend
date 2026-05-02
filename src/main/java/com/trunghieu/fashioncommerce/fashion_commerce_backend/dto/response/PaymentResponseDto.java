package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentStatus; // Import PaymentStatus
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentMethod; // Import PaymentMethod enum
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
public class PaymentResponseDto {
    private Long id;
    private BigDecimal amount;
    private PaymentMethod method; // Đã thay đổi từ String sang PaymentMethod enum
    private PaymentStatus status; // Đã thay đổi từ String sang PaymentStatus
    private String transactionCode;
    private LocalDateTime createdAt;
}
