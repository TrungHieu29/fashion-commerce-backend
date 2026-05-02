package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponseDto {
    private Long id;
    private Long productVariantId;
    private String productName;
    private String size;
    private String color;
    private Integer quantity;
    private BigDecimal price; // Giá tại thời điểm xem giỏ hàng
    private String imageUrl;
}