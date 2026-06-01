package com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums;

public enum OrderStatus {
    PENDING,
    CONFIRMED, // Thêm trạng thái CONFIRMED
    PROCESSING,
    SHIPPED,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    RETURN_REQUESTED,
    RETURNED
}
