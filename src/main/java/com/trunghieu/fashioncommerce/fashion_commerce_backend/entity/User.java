package com.trunghieu.fashioncommerce.fashion_commerce_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String username;

    @ToString.Exclude
    private String passwordHash;

    private String fullName;
    private String phone;
    private Integer status;
    private String email;
    private String avatar;
    private String gender;
    private LocalDateTime dateOfBirth;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<ShippingAddress> shippingAddresses;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Cart cart;

    @OneToOne(mappedBy = "owner", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Shop shop;

    // Đã xóa Set<Order> và Set<Review> để tránh performance issue và infinite loop.
    // Truy vấn thông qua Repository khi cần.

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}