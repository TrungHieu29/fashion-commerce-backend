package com.trunghieu.fashioncommerce.fashion_commerce_backend.repository;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderShop;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus; // Import OrderStatus
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderShopRepository extends JpaRepository<OrderShop, Long> {
    Page<OrderShop> findByOrderId(Long orderId, Pageable pageable);
    Page<OrderShop> findByShopId(Long shopId, Pageable pageable);
    Page<OrderShop> findByShopIdAndStatus(Long shopId, OrderStatus status, Pageable pageable); // Thêm phương thức mới
}
