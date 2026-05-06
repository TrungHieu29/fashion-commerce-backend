package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ProductVariantRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductVariantResponseDto;
import java.util.List;

public interface ProductVariantService {
    ProductVariantResponseDto createProductVariant(ProductVariantRequestDto requestDto);
    ProductVariantResponseDto getProductVariantById(Long id);
    List<ProductVariantResponseDto> getProductVariantsByProductId(Long productId);
    List<ProductVariantResponseDto> getAllProductVariants();
    ProductVariantResponseDto updateProductVariant(Long id, ProductVariantRequestDto requestDto);
    void deleteProductVariant(Long id);
}
