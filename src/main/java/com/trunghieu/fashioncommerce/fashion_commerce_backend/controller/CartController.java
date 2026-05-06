package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.CartItemRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.CartResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.security.CustomUserDetails;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    // ADMIN có thể xem giỏ hàng của bất kỳ user nào. CUSTOMER chỉ có thể xem giỏ hàng của chính mình.
    public ResponseEntity<CartResponseDto> getCartByUserId(@PathVariable Long userId) {
        CartResponseDto cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/user/{userId}/items")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    // ADMIN có thể thêm sản phẩm vào giỏ hàng của bất kỳ user nào. CUSTOMER chỉ có thể thêm vào giỏ hàng của chính mình.
    public ResponseEntity<CartResponseDto> addProductToCart(
            @PathVariable Long userId,
            @Valid @RequestBody CartItemRequestDto cartItemRequestDto) {
        CartResponseDto updatedCart = cartService.addProductToCart(userId, cartItemRequestDto);
        return new ResponseEntity<>(updatedCart, HttpStatus.OK);
    }

    @PutMapping("/user/{userId}/items/{cartItemId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    // ADMIN có thể cập nhật số lượng sản phẩm trong giỏ hàng của bất kỳ user nào. CUSTOMER chỉ có thể cập nhật giỏ hàng của chính mình.
    public ResponseEntity<CartResponseDto> updateCartItemQuantity(
            @PathVariable Long userId,
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        CartResponseDto updatedCart = cartService.updateCartItemQuantity(userId, cartItemId, quantity);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/user/{userId}/items/{cartItemId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    // ADMIN có thể xóa sản phẩm khỏi giỏ hàng của bất kỳ user nào. CUSTOMER chỉ có thể xóa khỏi giỏ hàng của chính mình.
    public ResponseEntity<Void> removeCartItem(
            @PathVariable Long userId,
            @PathVariable Long cartItemId) {
        cartService.removeCartItem(userId, cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    // ADMIN có thể xóa toàn bộ giỏ hàng của bất kỳ user nào. CUSTOMER chỉ có thể xóa giỏ hàng của chính mình.
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
