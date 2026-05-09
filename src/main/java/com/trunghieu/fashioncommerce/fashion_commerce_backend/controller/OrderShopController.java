package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderShopRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderShopResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.OrderShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order-shops")
@RequiredArgsConstructor
public class OrderShopController {

    private final OrderShopService orderShopService;

    @PostMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isShopOwner(#requestDto.shopId)")
    public ResponseEntity<OrderShopResponseDto> createOrderShop(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderShopRequestDto requestDto) {
        return new ResponseEntity<>(orderShopService.createOrderShop(orderId, requestDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOrderShopOwner(#id)")
    public ResponseEntity<OrderShopResponseDto> getOrderShopById(@PathVariable Long id) {
        return ResponseEntity.ok(orderShopService.getOrderShopById(id));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOrderOwner(#orderId)")
    public ResponseEntity<Page<OrderShopResponseDto>> getOrderShopsByOrderId(
            @PathVariable Long orderId,
            Pageable pageable) {
        return ResponseEntity.ok(orderShopService.getOrderShopsByOrderId(orderId, pageable));
    }

    @GetMapping("/shop/{shopId}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isShopOwner(#shopId)")
    public ResponseEntity<Page<OrderShopResponseDto>> getOrderShopsByShopId(
            @PathVariable Long shopId,
            Pageable pageable) {
        return ResponseEntity.ok(orderShopService.getOrderShopsByShopId(shopId, pageable));
    }
}
