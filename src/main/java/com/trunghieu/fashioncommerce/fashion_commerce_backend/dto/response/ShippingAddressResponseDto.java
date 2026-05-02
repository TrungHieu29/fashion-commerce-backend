package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddressResponseDto {
    private Long id;
    private Long userId;
    private String receiverName;
    private String phone;
    private String addressLine;
    private String city;
    private String district;
    private Boolean isDefault;
}