package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageRequestDto {
    private Long productId;
    private String color;
    private String imageUrl;
}