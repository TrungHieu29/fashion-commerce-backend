package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderShopRequestDto {
    @NotNull(message = "Shop ID cannot be null")
    private Long shopId;

    private Long discountId; // Có thể null nếu không áp dụng discount

    @NotNull(message = "Order items cannot be null")
    private List<OrderItemRequestDto> orderItems;
}