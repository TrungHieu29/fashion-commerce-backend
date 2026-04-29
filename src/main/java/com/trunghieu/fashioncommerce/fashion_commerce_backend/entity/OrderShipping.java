package com.trunghieu.fashioncommerce.fashion_commerce_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_shipping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderShipping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_shipping_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_shop_id")
    @ToString.Exclude
    private OrderShop orderShop;

    @Column(name = "address_snapshot", columnDefinition = "TEXT")
    private String addressSnapshot;

    @Column(name = "shipping_fee")
    private BigDecimal shippingFee;

    @Column(name = "shipping_status")
    private String shippingStatus;

    @Column(name = "tracking_code")
    private String trackingCode;
}