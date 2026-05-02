package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentMethod; // Import PaymentMethod enum
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentStatus; // Import PaymentStatus enum
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.0", message = "Amount cannot be negative")
    private BigDecimal amount;

    @NotNull(message = "Payment method cannot be blank") // Đã thay đổi từ NotBlank sang NotNull vì là enum
    private PaymentMethod method; // Đã thay đổi từ String sang PaymentMethod enum

    private PaymentStatus status; // Đã thay đổi từ String sang PaymentStatus enum
    private String transactionCode; // Thường được nhận từ Gateway
}
