package com.trunghieu.fashioncommerce.fashion_commerce_backend.repository;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByEmail(String email);
    void deleteByEmail(String email);
}