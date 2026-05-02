package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.ShopStatus; // Import ShopStatus
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopResponseDto {
    private Long id;
    private String shopName;
    private String logo;
    private String phone;
    private ShopStatus status; // Đã thay đổi từ Integer sang ShopStatus
    private String address;
    private String email;
    private LocalDateTime createdAt;
    private Long ownerId;
    private String ownerFullName;
}
