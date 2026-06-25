package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.WishlistItemResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Product;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.User;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.WishlistItem;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.ProductMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.UserRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.WishlistItemRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<WishlistItemResponseDto> getWishlist(Long userId, Pageable pageable) {
        ensureUserExists(userId);
        return wishlistItemRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional
    public WishlistItemResponseDto addProduct(Long userId, Long productId) {
        return wishlistItemRepository.findByUserIdAndProductId(userId, productId)
                .map(this::toDto)
                .orElseGet(() -> createWishlistItem(userId, productId));
    }

    @Override
    @Transactional
    public void removeProduct(Long userId, Long productId) {
        if (!wishlistItemRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new ResourceNotFoundException("Wishlist item not found for userId: " + userId + ", productId: " + productId);
        }
        wishlistItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWishlisted(Long userId, Long productId) {
        return wishlistItemRepository.existsByUserIdAndProductId(userId, productId);
    }

    private WishlistItemResponseDto createWishlistItem(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        WishlistItem wishlistItem = WishlistItem.builder()
                .user(user)
                .product(product)
                .build();

        try {
            return toDto(wishlistItemRepository.save(wishlistItem));
        } catch (DataIntegrityViolationException ex) {
            return wishlistItemRepository.findByUserIdAndProductId(userId, productId)
                    .map(this::toDto)
                    .orElseThrow(() -> ex);
        }
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    private WishlistItemResponseDto toDto(WishlistItem wishlistItem) {
        return WishlistItemResponseDto.builder()
                .id(wishlistItem.getId())
                .userId(wishlistItem.getUser().getId())
                .productId(wishlistItem.getProduct().getId())
                .product(productMapper.toDto(wishlistItem.getProduct()))
                .createdAt(wishlistItem.getCreatedAt())
                .build();
    }
}
