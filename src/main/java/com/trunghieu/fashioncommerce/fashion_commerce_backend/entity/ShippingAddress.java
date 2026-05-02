package com.trunghieu.fashioncommerce.fashion_commerce_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime; // Import LocalDateTime

@Entity
@Table(name = "shipping_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    @Column(name = "receiver_name")
    private String receiverName;

    @Column(name = "phone") // Added @Column
    private String phone;

    @Column(name = "address_line")
    private String addressLine;

    @Column(name = "city") // Added @Column
    private String city;

    @Column(name = "district") // Added @Column
    private String district;

    @Column(name = "is_default")
    private Boolean isDefault;

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
