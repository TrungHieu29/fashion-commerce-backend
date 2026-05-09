package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderItemRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderItemResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.OrderItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService orderItemService;

    @PostMapping("/order-shop/{orderShopId}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOrderShopOwner(#orderShopId)")
    public ResponseEntity<OrderItemResponseDto> createOrderItem(
            @PathVariable Long orderShopId,
            @Valid @RequestBody OrderItemRequestDto requestDto) {
        return new ResponseEntity<>(orderItemService.createOrderItem(orderShopId, requestDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOrderItemOwner(#id)")
    public ResponseEntity<OrderItemResponseDto> getOrderItemById(@PathVariable Long id) {
        return ResponseEntity.ok(orderItemService.getOrderItemById(id));
    }

    @GetMapping("/order-shop/{orderShopId}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOrderShopOwner(#orderShopId)")
    public ResponseEntity<Page<OrderItemResponseDto>> getOrderItemsByOrderShopId(
            @PathVariable Long orderShopId,
            Pageable pageable) {
        return ResponseEntity.ok(orderItemService.getOrderItemsByOrderShopId(orderShopId, pageable));
    }

    @GetMapping("/product-variant/{productVariantId}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isProductVariantOwner(#productVariantId)")
    public ResponseEntity<Page<OrderItemResponseDto>> getOrderItemsByProductVariantId(
            @PathVariable Long productVariantId,
            Pageable pageable) {
        return ResponseEntity.ok(orderItemService.getOrderItemsByProductVariantId(productVariantId, pageable));
    }
}
