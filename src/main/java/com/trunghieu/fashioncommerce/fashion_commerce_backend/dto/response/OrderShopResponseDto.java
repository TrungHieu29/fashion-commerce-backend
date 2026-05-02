package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderShopResponseDto {
    private Long id;
    private Long orderId;
    private Long shopId;
    private String shopName;
    private BigDecimal totalPrice;
    private BigDecimal finalPrice;
    private Long discountId;
    private Set<OrderItemResponseDto> orderItems;
    private OrderShippingResponseDto shipping;
}