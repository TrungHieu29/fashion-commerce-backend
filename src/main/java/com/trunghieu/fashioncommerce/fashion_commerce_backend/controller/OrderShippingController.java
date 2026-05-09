package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderShippingRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderShippingResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.OrderShippingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order-shippings")
@RequiredArgsConstructor
public class OrderShippingController {

    private final OrderShippingService orderShippingService;

    @PostMapping("/order-shop/{orderShopId}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOrderShopOwner(#orderShopId)")
    public ResponseEntity<OrderShippingResponseDto> createOrderShipping(
            @PathVariable Long orderShopId,
            @Valid @RequestBody OrderShippingRequestDto requestDto) {
        return new ResponseEntity<>(orderShippingService.createOrderShipping(orderShopId, requestDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOrderShippingOwner(#id)")
    public ResponseEntity<OrderShippingResponseDto> getOrderShippingById(@PathVariable Long id) {
        return ResponseEntity.ok(orderShippingService.getOrderShippingById(id));
    }

    @GetMapping("/order-shop/{orderShopId}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOrderShopOwner(#orderShopId)")
    public ResponseEntity<OrderShippingResponseDto> getOrderShippingByOrderShopId(@PathVariable Long orderShopId) {
        return ResponseEntity.ok(orderShippingService.getOrderShippingByOrderShopId(orderShopId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOrderShippingOwner(#id)")
    public ResponseEntity<OrderShippingResponseDto> updateOrderShipping(
            @PathVariable Long id,
            @Valid @RequestBody OrderShippingRequestDto requestDto) {
        return ResponseEntity.ok(orderShippingService.updateOrderShipping(id, requestDto));
    }
}
