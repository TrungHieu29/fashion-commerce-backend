package com.trunghieu.fashioncommerce.fashion_commerce_backend.repository;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Discount;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.DiscountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
    java.util.List<Discount> findByShopIdAndStatusAndStartDateBeforeAndEndDateAfter(Long shopId, DiscountStatus status,
            java.time.LocalDateTime start, java.time.LocalDateTime end);
    Page<Discount> findByShopId(Long shopId, Pageable pageable);
    Optional<Discount> findByShopIdAndCodeAndStatus(Long shopId, String code, DiscountStatus status);
}