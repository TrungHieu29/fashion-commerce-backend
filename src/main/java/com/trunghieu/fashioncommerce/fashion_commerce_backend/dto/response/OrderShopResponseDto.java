package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus;

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
    private String addressSnapshot;
    private OrderStatus status;
    private Set<OrderItemResponseDto> orderItems;
    private OrderShippingResponseDto shipping;
}