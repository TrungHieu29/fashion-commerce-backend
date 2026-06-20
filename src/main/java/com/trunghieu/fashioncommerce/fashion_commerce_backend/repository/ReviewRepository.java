package com.trunghieu.fashioncommerce.fashion_commerce_backend.repository;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProductId(Long productId, Pageable pageable);
    Page<Review> findByUserId(Long userId, Pageable pageable);
    @Query("""
        SELECT AVG(r.rating)
        FROM Review r
        WHERE r.product.id = :productId
    """)
    Double calculateAverageRatingByProductId(
            @Param("productId") Long productId
    );
}