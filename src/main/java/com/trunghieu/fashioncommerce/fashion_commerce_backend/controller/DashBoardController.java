package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.cloudinary.api.ApiResponse;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ShopDashboardResponse;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashBoardController {

    private final DashboardService dashboardService;

    @GetMapping("/shop/{shopId}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isShopOwner(#shopId)")
    public ResponseEntity<ShopDashboardResponse> getDashboard(
            @PathVariable Long shopId
    ) {
        return ResponseEntity.ok(
                dashboardService.getDashboard(shopId)
        );
    }
}