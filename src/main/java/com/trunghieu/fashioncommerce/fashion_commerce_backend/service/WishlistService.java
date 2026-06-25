package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.WishlistItemResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WishlistService {
    Page<WishlistItemResponseDto> getWishlist(Long userId, Pageable pageable);

    WishlistItemResponseDto addProduct(Long userId, Long productId);

    void removeProduct(Long userId, Long productId);

    boolean isWishlisted(Long userId, Long productId);
}
