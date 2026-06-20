package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderStatusStatisticResponse;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.RecentOrderResponse;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ShopDashboardResponse;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderShop;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.ProductStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.OrderShopRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductVariantRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl
        implements DashboardService {

    private final OrderShopRepository orderShopRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public ShopDashboardResponse getDashboard(
            Long shopId
    ) {

        BigDecimal revenue =
                orderShopRepository.getTotalRevenue(shopId);

        long totalOrders =
                orderShopRepository.countByShopId(shopId);

        long pendingOrders =
                orderShopRepository.countByShopIdAndStatusIn(
                        shopId,
                        List.of(
                                OrderStatus.PENDING,
                                OrderStatus.CONFIRMED,
                                OrderStatus.PROCESSING
                        )
                );
        long activeProducts =
                productRepository.countByShopIdAndStatus(
                        shopId,
                        ProductStatus.ACTIVE
                );
        Long lowStockProducts =
                (long) productVariantRepository
                        .findLowStockProductIds(shopId)
                        .size();
        List<OrderShop> recentOrders =
                orderShopRepository
                        .findTop5ByShopIdOrderByIdDesc(shopId);
        List<RecentOrderResponse> recentOrderResponses =
                recentOrders.stream()
                        .map(orderShop -> RecentOrderResponse.builder()
                                .orderShopId(orderShop.getId())
                                .orderId(orderShop.getOrder().getId())
                                .customerName(
                                        orderShop.getOrder()
                                                .getUser()
                                                .getFullName()
                                )
                                .status(orderShop.getStatus().name())
                                .finalPrice(orderShop.getFinalPrice())
                                .createdAt(
                                        orderShop.getOrder()
                                                .getCreatedAt()
                                )
                                .build())
                        .toList();
        List<OrderStatusStatisticResponse> statistics =
                Arrays.stream(OrderStatus.values())
                        .map(status ->
                                OrderStatusStatisticResponse.builder()
                                        .status(status.name())
                                        .count(
                                                orderShopRepository
                                                        .countByShopIdAndStatus(
                                                                shopId,
                                                                status
                                                        )
                                        )
                                        .build()
                        )
                        .toList();
        return ShopDashboardResponse.builder()
                .totalRevenue(revenue)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .activeProducts(activeProducts)
                .lowStockProducts(lowStockProducts)
                .recentOrders(recentOrderResponses)
                .orderStatusStatistics(statistics)
                .build();
    }
}