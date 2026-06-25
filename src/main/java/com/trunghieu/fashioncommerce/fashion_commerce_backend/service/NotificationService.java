package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.NotificationResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderShop;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    Page<NotificationResponseDto> getUserNotifications(Long userId, Boolean isRead, Pageable pageable);

    long countUnread(Long userId);

    NotificationResponseDto markAsRead(Long notificationId);

    void markAllAsRead(Long userId);

    NotificationResponseDto createOrderNotification(
            OrderShop orderShop,
            NotificationType type,
            String title,
            String message
    );
}
