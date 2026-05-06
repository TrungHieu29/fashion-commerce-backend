package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ShippingAddressRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ShippingAddressResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.security.CustomUserDetails;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ShippingAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipping-addresses")
@RequiredArgsConstructor
public class ShippingAddressController {

    private final ShippingAddressService shippingAddressService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or (#requestDto.userId == authentication.principal.id)")
    // ADMIN có thể tạo địa chỉ cho bất kỳ user nào. CUSTOMER chỉ có thể tạo địa chỉ cho chính mình.
    public ResponseEntity<ShippingAddressResponseDto> createShippingAddress(@Valid @RequestBody ShippingAddressRequestDto requestDto, Authentication authentication) {
        // Kiểm tra bổ sung nếu là CUSTOMER, đảm bảo userId trong request khớp với ID của người dùng đang đăng nhập
        if (authentication.getPrincipal() instanceof CustomUserDetails && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            if (!currentUser.getId().equals(requestDto.getUserId())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN); // Hoặc ném AccessDeniedException
            }
        }
        ShippingAddressResponseDto createdAddress = shippingAddressService.createShippingAddress(requestDto);
        return new ResponseEntity<>(createdAddress, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isShippingAddressOwner(#id)")
    // ADMIN có thể xem bất kỳ địa chỉ nào. Chủ địa chỉ chỉ có thể xem địa chỉ của mình.
    public ResponseEntity<ShippingAddressResponseDto> getShippingAddressById(@PathVariable Long id) {
        ShippingAddressResponseDto address = shippingAddressService.getShippingAddressById(id);
        return ResponseEntity.ok(address);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    // ADMIN có thể xem địa chỉ của bất kỳ user nào. CUSTOMER chỉ có thể xem địa chỉ của chính mình.
    public ResponseEntity<List<ShippingAddressResponseDto>> getShippingAddressesByUserId(@PathVariable Long userId, Authentication authentication) {
        List<ShippingAddressResponseDto> addresses = shippingAddressService.getShippingAddressesByUserId(userId);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN mới được xem tất cả địa chỉ
    public ResponseEntity<List<ShippingAddressResponseDto>> getAllShippingAddresses() {
        List<ShippingAddressResponseDto> addresses = shippingAddressService.getAllShippingAddresses();
        return ResponseEntity.ok(addresses);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isShippingAddressOwner(#id)")
    // ADMIN có thể cập nhật bất kỳ địa chỉ nào. Chủ địa chỉ chỉ có thể cập nhật địa chỉ của mình.
    public ResponseEntity<ShippingAddressResponseDto> updateShippingAddress(@PathVariable Long id, @Valid @RequestBody ShippingAddressRequestDto requestDto) {
        ShippingAddressResponseDto updatedAddress = shippingAddressService.updateShippingAddress(id, requestDto);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isShippingAddressOwner(#id)")
    // ADMIN có thể xóa bất kỳ địa chỉ nào. Chủ địa chỉ chỉ có thể xóa địa chỉ của mình.
    public ResponseEntity<Void> deleteShippingAddress(@PathVariable Long id) {
        shippingAddressService.deleteShippingAddress(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/set-default")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isShippingAddressOwner(#id)")
    // ADMIN có thể đặt mặc định cho bất kỳ địa chỉ nào. Chủ địa chỉ chỉ có thể đặt mặc định cho địa chỉ của mình.
    public ResponseEntity<ShippingAddressResponseDto> setDefaultShippingAddress(@PathVariable Long id, Authentication authentication) {
        CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
        ShippingAddressResponseDto updatedAddress = shippingAddressService.setDefaultShippingAddress(id, currentUser.getId());
        return ResponseEntity.ok(updatedAddress);
    }
}
