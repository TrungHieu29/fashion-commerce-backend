package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyVoucherRequestDto {
    @NotNull(message = "Shop ID cannot be null")
    private Long shopId;

    @NotBlank(message = "Voucher code cannot be blank")
    private String voucherCode;

    @NotNull(message = "Subtotal cannot be null")
    private BigDecimal subtotal;
}
