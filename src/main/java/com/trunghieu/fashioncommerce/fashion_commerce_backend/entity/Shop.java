package com.trunghieu.fashioncommerce.fashion_commerce_backend.entity;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.ShopStatus; // Import ShopStatus
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

    @Column(name = "logo") // Added @Column
    private String logo;

    @Column(name = "phone") // Added @Column
    private String phone;

    @Enumerated(EnumType.STRING) // Add this annotation
    @Column(name = "status") // Added @Column
    private ShopStatus status; // Change type to ShopStatus

    @Column(name = "address") // Added @Column
    private String address;

    @Column(name = "email") // Added @Column
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at") // Added updatedAt field
    private LocalDateTime updatedAt;

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
        updatedAt = LocalDateTime.now(); // Initialize updatedAt on creation
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(); // Update updatedAt on update
    }
}
