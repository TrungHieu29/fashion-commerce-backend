package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.WishlistItemResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<Page<WishlistItemResponseDto>> getWishlist(
            @PathVariable Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(wishlistService.getWishlist(userId, pageable));
    }

    @PostMapping("/user/{userId}/products/{productId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<WishlistItemResponseDto> addProduct(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        return new ResponseEntity<>(wishlistService.addProduct(userId, productId), HttpStatus.CREATED);
    }

    @DeleteMapping("/user/{userId}/products/{productId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<Void> removeProduct(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        wishlistService.removeProduct(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}/products/{productId}/exists")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<Boolean> isWishlisted(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(wishlistService.isWishlisted(userId, productId));
    }
}
