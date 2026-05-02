package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.DiscountStatus; // Import DiscountStatus
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountRequestDto {
    @NotNull(message = "Shop ID is required")
    private Long shopId;
    @NotBlank(message = "Discount type is required")
    private String discountType; // PERCENT or FIXED
    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal discountValue;
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    @NotNull(message = "End date is required")
    private LocalDateTime endDate;
    @NotNull(message = "Status is required")
    private DiscountStatus status; // Đã thay đổi từ Integer sang DiscountStatus
    private BigDecimal minOrderValue;
    private Set<Long> productIds; // Danh sách sản phẩm áp dụng
}
