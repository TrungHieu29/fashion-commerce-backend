package com.trunghieu.fashioncommerce.fashion_commerce_backend.repository;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Product;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = {"shop", "brand"})
    @Query("""
       SELECT DISTINCT p
       FROM Product p
       JOIN p.categories c
       WHERE c.id = :categoryId
       """)
    Page<Product> findByCategoryId(
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );
    @EntityGraph(attributePaths = {"shop", "brand"})
    Page<Product> findByShopId(Long shopId, Pageable pageable);
    @EntityGraph(attributePaths = {"shop", "brand"})
    Page<Product> findByBrandId(Long brandId, Pageable pageable);
    @EntityGraph(attributePaths = {"shop", "brand"})
    Page<Product> findByProductNameContainingIgnoreCase(String name, Pageable pageable);
    long countByShopIdAndStatus(
            Long shopId,
            ProductStatus status
    );
    @Query("""
SELECT p FROM Product p
WHERE p.shop.status = com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.ShopStatus.ACTIVE
""")
    @EntityGraph(attributePaths = {"shop", "brand"})
    Page<Product> findAllActive(Pageable pageable);
}
