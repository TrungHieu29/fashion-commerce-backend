package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopRequestDto {
    @NotBlank(message = "Shop name is required")
    private String shopName;
    private String logo;
    @NotBlank(message = "Phone is required")
    private String phone;
    private String address;
    @Email(message = "Invalid email")
    private String email;
    private Long ownerId;
}