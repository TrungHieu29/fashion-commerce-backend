package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.CartItemRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.CartResponseDto;

public interface CartService {
    CartResponseDto getCartByUserId(Long userId);
    CartResponseDto addProductToCart(Long userId, CartItemRequestDto cartItemRequestDto);
    CartResponseDto updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity);
    void removeCartItem(Long userId, Long cartItemId);
    void clearCart(Long userId);
}
