package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductImageResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isProductOwner(#productId)")
    public ResponseEntity<ProductImageResponseDto> uploadImage(
            @RequestParam Long productId,
            @RequestParam String color,
            @RequestParam MultipartFile file) throws IOException {
        return ResponseEntity.ok(productImageService.uploadProductImage(productId, color, file));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isProductOwner(@productImageRepository.findById(#id).get().product.id)")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) throws IOException {
        productImageService.deleteProductImage(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductImageResponseDto>> getImagesByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productImageService.getImagesByProductId(productId));
    }
}