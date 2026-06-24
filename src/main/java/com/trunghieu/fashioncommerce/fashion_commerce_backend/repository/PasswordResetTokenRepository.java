package com.trunghieu.fashioncommerce.fashion_commerce_backend.repository;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByEmail(String email);

    void deleteByEmail(String email);

    void deleteByExpiryDateBefore(LocalDateTime time);

    boolean existsByEmail(String email);
}
