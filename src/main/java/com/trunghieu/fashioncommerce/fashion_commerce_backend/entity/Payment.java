package com.trunghieu.fashioncommerce.fashion_commerce_backend.entity;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentMethod; // Import PaymentMethod
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentStatus; // Import PaymentStatus
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @ToString.Exclude
    private Order order;

    @Column(name = "amount") // Added @Column
    private BigDecimal amount;

    @Enumerated(EnumType.STRING) // Add this annotation
    @Column(name = "method") // Added @Column
    private PaymentMethod method; // Change type to PaymentMethod

    @Enumerated(EnumType.STRING) // Add this annotation
    @Column(name = "status") // Added @Column
    private PaymentStatus status; // Change type to PaymentStatus

    @Column(name = "transaction_code")
    private String transactionCode;

    @Column(name = "created_at") // Added @Column
    private LocalDateTime createdAt;

    @Column(name = "updated_at") // Added updatedAt field
    private LocalDateTime updatedAt;

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
