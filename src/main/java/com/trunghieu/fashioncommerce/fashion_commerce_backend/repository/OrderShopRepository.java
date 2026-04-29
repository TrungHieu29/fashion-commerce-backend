package com.trunghieu.fashioncommerce.fashion_commerce_backend.repository;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderShop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderShopRepository extends JpaRepository<OrderShop, Long> {
    Page<OrderShop> findByOrderId(Long orderId, Pageable pageable);
    Page<OrderShop> findByShopId(Long shopId, Pageable pageable);
}