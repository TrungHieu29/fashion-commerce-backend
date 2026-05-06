package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ProductRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductResponseDto createProduct(ProductRequestDto requestDto);
    ProductResponseDto getProductById(Long id);
    Page<ProductResponseDto> getAllProducts(Pageable pageable);
    Page<ProductResponseDto> getProductsByShopId(Long shopId, Pageable pageable);
    Page<ProductResponseDto> getProductsByCategoryId(Long categoryId, Pageable pageable);
    Page<ProductResponseDto> getProductsByBrandId(Long brandId, Pageable pageable);
    Page<ProductResponseDto> searchProducts(String keyword, Pageable pageable);
    ProductResponseDto updateProduct(Long id, ProductRequestDto requestDto);
    void deleteProduct(Long id);
}
