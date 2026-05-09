package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.PaymentRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.PaymentResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.PaymentStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOrderOwner(#orderId)")
    public ResponseEntity<PaymentResponseDto> createPayment(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentRequestDto requestDto) {
        return new ResponseEntity<>(paymentService.createPayment(orderId, requestDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isPaymentOwner(#id)")
    public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOrderOwner(#orderId)")
    public ResponseEntity<PaymentResponseDto> getPaymentByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponseDto> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam PaymentStatus status) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(id, status));
    }
}
