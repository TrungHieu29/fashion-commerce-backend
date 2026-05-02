package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.ShippingStatus; // Import ShippingStatus
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderShippingResponseDto {
    private Long id;
    private String addressSnapshot;
    private BigDecimal shippingFee;
    private ShippingStatus shippingStatus; // Đã thay đổi từ String sang ShippingStatus
    private String trackingCode;
}
