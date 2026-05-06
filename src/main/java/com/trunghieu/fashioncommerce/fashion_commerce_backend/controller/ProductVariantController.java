package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ProductVariantRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductVariantResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.security.CustomUserDetails;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isProductOwner(#requestDto.productId)")
    // ADMIN có thể tạo biến thể cho bất kỳ sản phẩm nào. Chủ shop chỉ có thể tạo biến thể cho sản phẩm của shop mình.
    public ResponseEntity<ProductVariantResponseDto> createProductVariant(@Valid @RequestBody ProductVariantRequestDto requestDto, Authentication authentication) {
        ProductVariantResponseDto createdVariant = productVariantService.createProductVariant(requestDto);
        return new ResponseEntity<>(createdVariant, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')") // ADMIN và CUSTOMER đều có thể xem biến thể theo ID
    public ResponseEntity<ProductVariantResponseDto> getProductVariantById(@PathVariable Long id) {
        ProductVariantResponseDto variant = productVariantService.getProductVariantById(id);
        return ResponseEntity.ok(variant);
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')") // ADMIN và CUSTOMER đều có thể xem biến thể theo Product ID
    public ResponseEntity<List<ProductVariantResponseDto>> getProductVariantsByProductId(@PathVariable Long productId) {
        List<ProductVariantResponseDto> variants = productVariantService.getProductVariantsByProductId(productId);
        return ResponseEntity.ok(variants);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')") // ADMIN và CUSTOMER đều có thể xem tất cả biến thể
    public ResponseEntity<List<ProductVariantResponseDto>> getAllProductVariants() {
        List<ProductVariantResponseDto> variants = productVariantService.getAllProductVariants();
        return ResponseEntity.ok(variants);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isProductVariantOwner(#id)")
    // ADMIN có thể cập nhật bất kỳ biến thể nào. Chủ shop chỉ có thể cập nhật biến thể của sản phẩm thuộc shop mình.
    public ResponseEntity<ProductVariantResponseDto> updateProductVariant(@PathVariable Long id, @Valid @RequestBody ProductVariantRequestDto requestDto) {
        ProductVariantResponseDto updatedVariant = productVariantService.updateProductVariant(id, requestDto);
        return ResponseEntity.ok(updatedVariant);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isProductVariantOwner(#id)")
    // ADMIN có thể xóa bất kỳ biến thể nào. Chủ shop chỉ có thể xóa biến thể của sản phẩm thuộc shop mình.
    public ResponseEntity<Void> deleteProductVariant(@PathVariable Long id) {
        productVariantService.deleteProductVariant(id);
        return ResponseEntity.noContent().build();
    }
}
