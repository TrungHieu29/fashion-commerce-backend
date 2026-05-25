package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductImageResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Product;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductImage;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.ProductImageMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductImageRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.CloudinaryService;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;
    private final ProductImageMapper productImageMapper;

    @Override
    @Transactional
    public ProductImageResponseDto uploadProductImage(Long productId, String color, MultipartFile file)
            throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Map uploadResult = cloudinaryService.upload(file);
        String imageUrl = (String) uploadResult.get("url");
        String publicId = (String) uploadResult.get("public_id");

        Optional<ProductImage> existingImage = productImageRepository.findByProductIdAndColor(productId, color);
        ProductImage productImage;

        if (existingImage.isPresent()) {
            productImage = existingImage.get();
            // Xóa ảnh cũ trên Cloudinary trước khi cập nhật
            cloudinaryService.delete(productImage.getPublicId());
            productImage.setImageUrl(imageUrl);
            productImage.setPublicId(publicId);
        } else {
            productImage = ProductImage.builder()
                    .product(product)
                    .color(color)
                    .imageUrl(imageUrl)
                    .publicId(publicId)
                    .build();
        }

        return productImageMapper.toDto(productImageRepository.save(productImage));
    }

    @Override
    @Transactional
    public void deleteProductImage(Long id) throws IOException {
        ProductImage image = productImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));
        cloudinaryService.delete(image.getPublicId());
        productImageRepository.delete(image);
    }

    @Override
    public List<ProductImageResponseDto> getImagesByProductId(Long productId) {
        return productImageRepository.findByProductId(productId).stream()
                .map(productImageMapper::toDto)
                .collect(Collectors.toList());
    }
}