package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddressRequestDto {
    @NotNull(message = "User ID is required")
    private Long userId;
    @NotBlank(message = "Receiver name is required")
    private String receiverName;
    @NotBlank(message = "Phone is required")
    private String phone;
    @NotBlank(message = "Address line is required")
    private String addressLine;
    @NotBlank(message = "City is required")
    private String city;
    @NotBlank(message = "District is required")
    private String district;
    private Boolean isDefault = false;
}