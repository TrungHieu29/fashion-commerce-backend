package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDto {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    // Loại bỏ totalPrice và finalPrice vì sẽ tính tự động từ cart

    private Long addressId; // ID địa chỉ có sẵn
    private String receiverName; // Tên người nhận (nếu nhập mới)
    private String phone; // SĐT người nhận (nếu nhập mới)
    private String addressLine; // Địa chỉ chi tiết (nếu nhập mới)
    private String city; // Tỉnh/Thành phố (nếu nhập mới)
    private String district; // Quận/Huyện (nếu nhập mới)
}