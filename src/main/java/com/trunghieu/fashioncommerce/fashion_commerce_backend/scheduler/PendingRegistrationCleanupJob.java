package com.trunghieu.fashioncommerce.fashion_commerce_backend.scheduler;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.PasswordResetTokenRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.PendingRegistrationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PendingRegistrationCleanupJob {

    private final PendingRegistrationRepository pendingRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void cleanupExpiredRegistrations() {

        pendingRepository.deleteByExpiryDateBefore(
                LocalDateTime.now()
        );
        passwordResetTokenRepository
                .deleteByExpiryDateBefore(
                        LocalDateTime.now()
                );
        System.out.println(
                "[Scheduler] Cleaned expired pending registrations"
        );

    }
}