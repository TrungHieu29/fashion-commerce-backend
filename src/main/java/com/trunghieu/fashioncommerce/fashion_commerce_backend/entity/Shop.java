package com.trunghieu.fashioncommerce.fashion_commerce_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "shops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_id")
    private Long id;

    @Column(name = "shop_name", nullable = false)
    private String shopName;

    private String logo;
    private String phone;
    private Integer status;
    private String address;
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @ToString.Exclude
    private User owner;

    // Chỉ giữ lại những quan hệ thực sự cần thiết để quản lý theo cascade.
    // Product và Discount thường được quản lý độc lập qua Repository với phân trang.
    // Tuy nhiên, nếu bạn muốn giữ để xem danh sách sơ bộ, hãy để LAZY.
    
    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<OrderShop> orderShops;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}