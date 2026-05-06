package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ProductBrandRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductBrandResponseDto;
import java.util.List;

public interface ProductBrandService {
    ProductBrandResponseDto createProductBrand(ProductBrandRequestDto requestDto);
    ProductBrandResponseDto getProductBrandById(Long id);
    List<ProductBrandResponseDto> getAllProductBrands();
    ProductBrandResponseDto updateProductBrand(Long id, ProductBrandRequestDto requestDto);
    void deleteProductBrand(Long id);
}