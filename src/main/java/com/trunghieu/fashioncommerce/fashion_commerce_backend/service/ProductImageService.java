package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductImageResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductImageService {
    ProductImageResponseDto uploadProductImage(Long productId, String color, MultipartFile file) throws IOException;

    void deleteProductImage(Long id) throws IOException;

    List<ProductImageResponseDto> getImagesByProductId(Long productId);
}