package com.trunghieu.fashioncommerce.fashion_commerce_backend.repository;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.TimelineDataDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderShop;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus; // Import OrderStatus
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface OrderShopRepository extends JpaRepository<OrderShop, Long> {
    Page<OrderShop> findByOrderId(Long orderId, Pageable pageable);
    Page<OrderShop> findByShopId(Long shopId, Pageable pageable);
    Page<OrderShop> findByShopIdAndStatus(Long shopId, OrderStatus status, Pageable pageable);
    long countByShopId(Long shopId);// Thêm phương thức mới
    long countByShopIdAndStatusIn(
            Long shopId,
            Collection<OrderStatus> statuses
    );
    List<OrderShop> findTop5ByShopIdOrderByIdDesc(
            Long shopId
    );
    @Query("""
    SELECT COALESCE(SUM(os.finalPrice),0)
    FROM OrderShop os
    WHERE os.shop.id = :shopId
    AND os.status IN (
        com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus.COMPLETED
    )
""")
    BigDecimal getTotalRevenue(Long shopId);

    long countByShopIdAndStatus(
            Long shopId,
            OrderStatus status
    );
    @Query("SELECT os FROM OrderShop os " +
            "JOIN os.order o " +
            "WHERE os.shop.id = :shopId " +
            "AND os.status IN (com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus.DELIVERED, com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus.COMPLETED) " +
            "AND o.createdAt >= :startDate " +
            "AND o.createdAt <= :endDate " +
            "ORDER BY o.createdAt ASC")
    List<OrderShop> findAnalyticsOrders(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
