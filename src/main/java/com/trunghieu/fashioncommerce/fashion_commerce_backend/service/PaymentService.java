package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.PaymentRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.PaymentResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    PaymentResponseDto createPayment(Long orderId, PaymentRequestDto requestDto);
    PaymentResponseDto getPaymentById(Long id);
    PaymentResponseDto getPaymentByOrderId(Long orderId);
    PaymentResponseDto updatePaymentStatus(Long paymentId, PaymentStatus status);
}
