package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.DiscountRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.DiscountResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.DiscountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isShopOwner(#requestDto.shopId)")
    public ResponseEntity<DiscountResponseDto> createDiscount(@Valid @RequestBody DiscountRequestDto requestDto) {
        return new ResponseEntity<>(discountService.createDiscount(requestDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<DiscountResponseDto> getDiscountById(@PathVariable Long id) {
        return ResponseEntity.ok(discountService.getDiscountById(id));
    }

    @GetMapping("/shops/{shopId}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<Page<DiscountResponseDto>> getDiscountsByShop(
            @PathVariable Long shopId,
            Pageable pageable) {
        return ResponseEntity.ok(discountService.getDiscountsByShopId(shopId, pageable));
    }

    @GetMapping("/shops/{shopId}/active")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<List<DiscountResponseDto>> getActiveDiscountsByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(discountService.getActiveDiscountsByShopId(shopId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isDiscountShopOwner(#id)")
    public ResponseEntity<DiscountResponseDto> updateDiscount(
            @PathVariable Long id,
            @Valid @RequestBody DiscountRequestDto requestDto) {
        return ResponseEntity.ok(discountService.updateDiscount(id, requestDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isDiscountShopOwner(#id)")
    public ResponseEntity<Void> deleteDiscount(@PathVariable Long id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.noContent().build();
    }
}
