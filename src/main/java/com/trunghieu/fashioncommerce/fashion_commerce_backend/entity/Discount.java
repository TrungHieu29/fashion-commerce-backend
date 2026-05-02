package com.trunghieu.fashioncommerce.fashion_commerce_backend.entity;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.DiscountStatus; // Import DiscountStatus
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.DiscountType; // Import DiscountType
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "discounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discount_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    @ToString.Exclude
    private Shop shop;

    @Enumerated(EnumType.STRING) // Add this annotation
    @Column(name = "discount_type")
    private DiscountType discountType; // Change type to DiscountType

    @Column(name = "discount_value")
    private BigDecimal discountValue;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING) // Add this annotation
    @Column(name = "status") // Add @Column
    private DiscountStatus status; // Change type to DiscountStatus

    @Column(name = "min_order_value")
    private BigDecimal minOrderValue;

    @Column(name = "created_at") // Added createdAt
    private LocalDateTime createdAt;

    @Column(name = "updated_at") // Added updatedAt
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
        name = "discount_product",
        joinColumns = @JoinColumn(name = "discount_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @ToString.Exclude
    private Set<Product> products;

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
