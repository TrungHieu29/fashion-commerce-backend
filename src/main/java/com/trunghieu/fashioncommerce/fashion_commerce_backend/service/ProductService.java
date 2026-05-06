package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ProductRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductResponseDto;
import java.util.List;

public interface ProductService {
    ProductResponseDto createProduct(ProductRequestDto requestDto);
    ProductResponseDto getProductById(Long id);
    List<ProductResponseDto> getAllProducts();
    List<ProductResponseDto> getProductsByCategory(Long categoryId);
    List<ProductResponseDto> getProductsByShop(Long shopId);
    List<ProductResponseDto> getProductsByBrand(Long brandId);
    ProductResponseDto updateProduct(Long id, ProductRequestDto requestDto);
    void deleteProduct(Long id);
}