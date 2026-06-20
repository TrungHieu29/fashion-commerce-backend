package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ShopDashboardResponse {

    private BigDecimal totalRevenue;

    private Long totalOrders;

    private Long pendingOrders;

    private Long totalCustomers;

    private Long activeProducts;

    private Long lowStockProducts;

    private List<OrderStatusStatisticResponse> orderStatusStatistics;

    private List<RecentOrderResponse> recentOrders;
}