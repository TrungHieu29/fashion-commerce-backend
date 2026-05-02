package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationRequestDto {
    @NotNull(message = "User ID is required")
    private Long userId;
    @NotNull(message = "Shop ID is required")
    private Long shopId;
}