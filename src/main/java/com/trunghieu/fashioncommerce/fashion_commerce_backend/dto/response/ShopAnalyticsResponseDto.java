package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopAnalyticsResponseDto {
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private Double growthRate;
    private Long totalOrders;              // Thêm trường này cho giống các sàn lớn
    private Double conversionRate;         // Thêm tỷ lệ chuyển đổi mẫu
    private List<TimelineDataDto> timelineRevenue;
}
