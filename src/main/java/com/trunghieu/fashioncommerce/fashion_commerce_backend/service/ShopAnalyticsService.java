package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ShopAnalyticsResponseDto;

public interface ShopAnalyticsService {
    ShopAnalyticsResponseDto getShopAnalytics(Long shopId, String period);
}
