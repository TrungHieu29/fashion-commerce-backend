package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RecentOrderResponse {

    private Long orderShopId;

    private Long orderId;

    private String customerName;

    private String status;

    private BigDecimal finalPrice;

    private LocalDateTime createdAt;
}