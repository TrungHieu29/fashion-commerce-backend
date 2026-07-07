package com.trunghieu.fashioncommerce.fashion_commerce_backend.repository;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductId(Long productId);

    List<ProductImage> findByProductIdOrderByCreatedAtAscIdAsc(Long productId);

    List<ProductImage> findByProductIdInOrderByProductIdAscCreatedAtAscIdAsc(Collection<Long> productIds);

    void deleteByProductId(Long productId);

    Optional<ProductImage> findByProductIdAndColor(Long productId, String color);
}
