package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.CartItemRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.CartResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Cart;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.CartItem;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductVariant;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.User;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.CartMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.CartItemRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.CartRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductVariantRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.UserRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponseDto getCartByUserId(Long userId) {
        // Tìm hoặc tạo giỏ hàng cho người dùng
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCartForUser(userId));
        return cartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public CartResponseDto addProductToCart(Long userId, CartItemRequestDto cartItemRequestDto) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCartForUser(userId));

        ProductVariant productVariant = productVariantRepository.findById(cartItemRequestDto.getProductVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant not found with id: " + cartItemRequestDto.getProductVariantId()));

        // Kiểm tra số lượng trong kho
        if (productVariant.getStock() < cartItemRequestDto.getQuantity()) {
            throw new IllegalArgumentException("Not enough stock for product variant: " + productVariant.getId());
        }

        Optional<CartItem> existingCartItem = cart.getCartItems().stream()
                .filter(item -> item.getProductVariant().getId().equals(productVariant.getId()))
                .findFirst();

        if (existingCartItem.isPresent()) {
            // Cập nhật số lượng
            CartItem item = existingCartItem.get();
            int newQuantity = item.getQuantity() + cartItemRequestDto.getQuantity();
            if (productVariant.getStock() < newQuantity) {
                throw new IllegalArgumentException("Not enough stock for product variant: " + productVariant.getId() + " with requested total quantity.");
            }
            item.setQuantity(newQuantity);
            item.setUpdatedAt(LocalDateTime.now());
            cartItemRepository.save(item);
        } else {
            // Thêm mới CartItem
            CartItem newCartItem = CartItem.builder()
                    .cart(cart)
                    .productVariant(productVariant)
                    .quantity(cartItemRequestDto.getQuantity())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            cart.getCartItems().add(newCartItem);
            cartItemRepository.save(newCartItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart); // Cập nhật thời gian cập nhật của giỏ hàng
        return cartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public CartResponseDto updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user with id: " + userId));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart Item not found with id: " + cartItemId + " in user's cart."));

        ProductVariant productVariant = cartItem.getProductVariant();

        // Kiểm tra số lượng trong kho
        if (productVariant.getStock() < quantity) {
            throw new IllegalArgumentException("Not enough stock for product variant: " + productVariant.getId());
        }

        if (quantity <= 0) {
            // Nếu số lượng là 0 hoặc âm, xóa CartItem
            cart.getCartItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItem.setUpdatedAt(LocalDateTime.now());
            cartItemRepository.save(cartItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart); // Cập nhật thời gian cập nhật của giỏ hàng
        return cartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public void removeCartItem(Long userId, Long cartItemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user with id: " + userId));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart Item not found with id: " + cartItemId + " in user's cart."));

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart); // Cập nhật thời gian cập nhật của giỏ hàng
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user with id: " + userId));

        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart); // Cập nhật thời gian cập nhật của giỏ hàng
    }

    private Cart createNewCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Cart newCart = Cart.builder()
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .cartItems(new HashSet<>()) // Khởi tạo set rỗng
                .build();
        return cartRepository.save(newCart);
    }
}
