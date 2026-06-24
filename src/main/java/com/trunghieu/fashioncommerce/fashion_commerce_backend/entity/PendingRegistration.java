package com.trunghieu.fashioncommerce.fashion_commerce_backend.entity;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pending_registrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name")
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String avatar;

    @Column(name = "otp_code", nullable = false)
    private String otpCode;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_otp_sent_at")
    private LocalDateTime lastOtpSentAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}