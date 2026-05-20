package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.CartItemRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.UpdateCartItemVariantRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.CartResponseDto;

public interface CartService {
    CartResponseDto getCartByUserId(Long userId);

    CartResponseDto addProductToCart(Long userId, CartItemRequestDto cartItemRequestDto);

    CartResponseDto updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity);

    CartResponseDto updateCartItemVariant(
            Long userId,
            Long cartItemId,
            UpdateCartItemVariantRequestDto requestDto);

    void removeCartItem(Long userId, Long cartItemId);

    void clearCart(Long userId);
}
