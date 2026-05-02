package com.trunghieu.fashioncommerce.fashion_commerce_backend.entity;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.Gender; // Import Gender
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.UserStatus; // Import UserStatus
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

    @Column(name = "username", unique = true, nullable = false) // Added @Column
    private String username;

    @ToString.Exclude
    @Column(name = "password_hash", nullable = false) // Added @Column
    private String passwordHash;

    @Column(name = "full_name") // Added @Column
    private String fullName;

    @Column(name = "phone") // Added @Column
    private String phone;

    @Enumerated(EnumType.STRING) // Add this annotation
    @Column(name = "status") // Added @Column
    private UserStatus status; // Change type to UserStatus

    @Column(name = "email", unique = true) // Added @Column
    private String email;

    @Column(name = "avatar") // Added @Column
    private String avatar;

    @Enumerated(EnumType.STRING) // Add this annotation
    @Column(name = "gender") // Added @Column
    private Gender gender; // Change type to Gender

    @Column(name = "date_of_birth") // Added @Column
    private LocalDateTime dateOfBirth;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at") // Added updatedAt field
    private LocalDateTime updatedAt;

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
        updatedAt = LocalDateTime.now(); // Initialize updatedAt on creation
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(); // Update updatedAt on update
    }
}
