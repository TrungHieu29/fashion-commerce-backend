package com.trunghieu.fashioncommerce.fashion_commerce_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_shop_id")
    @ToString.Exclude
    private OrderShop orderShop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    @ToString.Exclude
    private ProductVariant productVariant;

    private Integer quantity;
    private BigDecimal price; // Giá tại thời điểm mua

    @OneToOne(mappedBy = "orderItem", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Review review;
}