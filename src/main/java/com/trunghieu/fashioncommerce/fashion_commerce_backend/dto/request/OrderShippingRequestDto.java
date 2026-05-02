package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request;

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
public class OrderShippingRequestDto {
    @NotBlank(message = "Address snapshot cannot be blank")
    private String addressSnapshot;

    @NotNull(message = "Shipping fee cannot be null")
    @DecimalMin(value = "0.0", message = "Shipping fee cannot be negative")
    private BigDecimal shippingFee;

    private String shippingStatus; // Có thể có giá trị mặc định hoặc được set trong service
    private String trackingCode; // Có thể được sinh ra trong service
}