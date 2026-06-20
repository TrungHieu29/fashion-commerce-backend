package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ShopDashboardResponse;

public interface DashboardService {
    ShopDashboardResponse getDashboard(Long shopId);
}
