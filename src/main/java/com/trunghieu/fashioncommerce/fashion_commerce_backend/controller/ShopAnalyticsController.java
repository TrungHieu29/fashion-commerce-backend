package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ShopAnalyticsResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ShopAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard/shop")
@CrossOrigin(origins = "*")
public class ShopAnalyticsController {

    private final ShopAnalyticsService shopAnalyticsService;

    public ShopAnalyticsController(ShopAnalyticsService shopAnalyticsService) {
        this.shopAnalyticsService = shopAnalyticsService;
    }

    @GetMapping("/{shopId}/analytics")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isShopOwner(#shopId)")
    public ResponseEntity<ShopAnalyticsResponseDto> getShopAnalytics(
            @PathVariable Long shopId,
            // Hứng tham số ?period= từ React Query (mặc định nếu FE không truyền là "today")
            @RequestParam(value = "period", defaultValue = "today") String period
    ) {
        // Truyền thêm biến period xuống tầng Service xử lý rẽ nhánh điều kiện
        ShopAnalyticsResponseDto response = shopAnalyticsService.getShopAnalytics(shopId, period);
        return ResponseEntity.ok(response);
    }
}