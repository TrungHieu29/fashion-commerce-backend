package com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums;

public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUND_INITIATED, // Thêm trạng thái REFUND_INITIATED
    REFUNDED
}
