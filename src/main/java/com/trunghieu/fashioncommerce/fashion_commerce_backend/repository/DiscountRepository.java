package com.trunghieu.fashioncommerce.fashion_commerce_backend.repository;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Discount;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.DiscountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
    java.util.List<Discount> findByShopIdAndStatusAndStartDateBeforeAndEndDateAfter(Long shopId, DiscountStatus status,
            java.time.LocalDateTime start, java.time.LocalDateTime end);
    @Query("""
            SELECT DISTINCT d
            FROM Discount d
            LEFT JOIN FETCH d.products
            WHERE d.shop.id IN :shopIds
            AND d.status = :status
            AND d.startDate < :now
            AND d.endDate > :now
            """)
    java.util.List<Discount> findActiveByShopIdsWithProducts(
            @Param("shopIds") Collection<Long> shopIds,
            @Param("status") DiscountStatus status,
            @Param("now") java.time.LocalDateTime now
    );
    Page<Discount> findByShopId(Long shopId, Pageable pageable);
    Optional<Discount> findByShopIdAndCodeAndStatus(Long shopId, String code, DiscountStatus status);
}
