package com.trunghieu.fashioncommerce.fashion_commerce_backend.entity;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.ProductStatus; // Import ProductStatus
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime; // Import LocalDateTime
import java.util.Set;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_detail", columnDefinition = "TEXT")
    private String productDetail;

    @Column(name = "rating") // Added @Column
    private Double rating;

    @Enumerated(EnumType.STRING) // Add this annotation
    @Column(name = "status") // Added @Column
    private ProductStatus status; // Change type to ProductStatus

    @Column(name = "price") // Added @Column
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    @ToString.Exclude
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    @ToString.Exclude
    private ProductBrand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    private Category category;

    // Các quan hệ composition (thành phần của product) thì nên giữ OneToMany để Cascade
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<ProductVariant> variants;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<ProductImage> images;

    @ManyToMany(mappedBy = "products")
    @ToString.Exclude
    private Set<Discount> discounts;

    @Column(name = "created_at") // Added createdAt
    private LocalDateTime createdAt;

    @Column(name = "updated_at") // Added updatedAt
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
