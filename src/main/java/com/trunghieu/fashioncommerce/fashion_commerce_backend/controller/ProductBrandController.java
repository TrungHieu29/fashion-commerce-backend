package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ProductBrandRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductBrandResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ProductBrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-brands")
@RequiredArgsConstructor
public class ProductBrandController {

    private final ProductBrandService productBrandService;

    @PostMapping
    public ResponseEntity<ProductBrandResponseDto> createProductBrand(@Valid @RequestBody ProductBrandRequestDto requestDto) {
        ProductBrandResponseDto createdProductBrand = productBrandService.createProductBrand(requestDto);
        return new ResponseEntity<>(createdProductBrand, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductBrandResponseDto> getProductBrandById(@PathVariable Long id) {
        ProductBrandResponseDto productBrand = productBrandService.getProductBrandById(id);
        return ResponseEntity.ok(productBrand);
    }

    @GetMapping
    public ResponseEntity<List<ProductBrandResponseDto>> getAllProductBrands() {
        List<ProductBrandResponseDto> productBrands = productBrandService.getAllProductBrands();
        return ResponseEntity.ok(productBrands);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductBrandResponseDto> updateProductBrand(
            @PathVariable Long id,
            @Valid @RequestBody ProductBrandRequestDto requestDto) {
        ProductBrandResponseDto updatedProductBrand = productBrandService.updateProductBrand(id, requestDto);
        return ResponseEntity.ok(updatedProductBrand);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductBrand(@PathVariable Long id) {
        productBrandService.deleteProductBrand(id);
        return ResponseEntity.noContent().build();
    }
}