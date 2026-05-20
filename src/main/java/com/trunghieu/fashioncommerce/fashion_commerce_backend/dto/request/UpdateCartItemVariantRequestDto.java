package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartItemVariantRequestDto {
    @NotNull(message = "Product variant ID is required")
    private Long productVariantId;
}
