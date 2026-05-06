package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ShopRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ShopResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.security.CustomUserDetails;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #requestDto.ownerId == authentication.principal.id)")
    // ADMIN có thể tạo shop cho bất kỳ ai. CUSTOMER chỉ có thể tạo shop cho chính mình.
    public ResponseEntity<ShopResponseDto> createShop(@Valid @RequestBody ShopRequestDto requestDto, Authentication authentication) {
        // Nếu là CUSTOMER, đảm bảo ownerId trong request khớp với ID của người dùng đang đăng nhập
        if (authentication.getPrincipal() instanceof CustomUserDetails && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            if (!currentUser.getId().equals(requestDto.getOwnerId())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN); // Hoặc ném AccessDeniedException
            }
        }
        ShopResponseDto createdShop = shopService.createShop(requestDto);
        return new ResponseEntity<>(createdShop, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')") // ADMIN và CUSTOMER đều có thể xem shop theo ID
    public ResponseEntity<ShopResponseDto> getShopById(@PathVariable Long id) {
        ShopResponseDto shop = shopService.getShopById(id);
        return ResponseEntity.ok(shop);
    }

    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasRole('ADMIN') or #ownerId == authentication.principal.id")
    // ADMIN có thể xem shop của bất kỳ ai. CUSTOMER chỉ có thể xem shop của chính mình.
    public ResponseEntity<ShopResponseDto> getShopByOwnerId(@PathVariable Long ownerId, Authentication authentication) {
        ShopResponseDto shop = shopService.getShopByOwnerId(ownerId);
        return ResponseEntity.ok(shop);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')") // ADMIN và CUSTOMER đều có thể xem tất cả shops
    public ResponseEntity<List<ShopResponseDto>> getAllShops() {
        List<ShopResponseDto> shops = shopService.getAllShops();
        return ResponseEntity.ok(shops);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isShopOwner(#id)")
    // ADMIN có thể cập nhật bất kỳ shop nào. Chủ shop chỉ có thể cập nhật shop của mình.
    public ResponseEntity<ShopResponseDto> updateShop(@PathVariable Long id, @Valid @RequestBody ShopRequestDto requestDto) {
        ShopResponseDto updatedShop = shopService.updateShop(id, requestDto);
        return ResponseEntity.ok(updatedShop);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isShopOwner(#id)")
    // ADMIN có thể xóa bất kỳ shop nào. Chủ shop chỉ có thể xóa shop của mình.
    public ResponseEntity<Void> deleteShop(@PathVariable Long id) {
        shopService.deleteShop(id);
        return ResponseEntity.noContent().build();
    }
}
