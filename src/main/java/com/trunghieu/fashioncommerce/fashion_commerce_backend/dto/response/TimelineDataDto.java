package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimelineDataDto {
    private String label; // Sẽ là Giờ, Ngày, hoặc Tháng tùy bộ lọc
    private BigDecimal revenue;
    private Long orderCount;
}