package com.trunghieu.fashioncommerce.fashion_commerce_backend.repository;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Discount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    Page<Discount> findByShopId(Long shopId, Pageable pageable);
    List<Discount> findByShopIdAndStatusAndStartDateBeforeAndEndDateAfter(Long shopId, Integer status, LocalDateTime now1, LocalDateTime now2);
}