package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ProductVariantRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductVariantResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Product;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductVariant;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.ProductVariantMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductVariantRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository; // Để kiểm tra product tồn tại
    private final ProductVariantMapper productVariantMapper;

    @Override
    @Transactional
    public ProductVariantResponseDto createProductVariant(ProductVariantRequestDto requestDto) {
        Product product = productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + requestDto.getProductId()));

        // Kiểm tra trùng lặp size và color cho cùng một sản phẩm
        if (productVariantRepository.findByProductIdAndSizeAndColor(
                requestDto.getProductId(), requestDto.getSize(), requestDto.getColor()).isPresent()) {
            throw new IllegalArgumentException("Product Variant with size '" + requestDto.getSize() + "' and color '" + requestDto.getColor() + "' already exists for product id: " + requestDto.getProductId());
        }

        ProductVariant productVariant = productVariantMapper.toEntity(requestDto);
        productVariant.setProduct(product); // Gán product

        productVariant = productVariantRepository.save(productVariant);
        return productVariantMapper.toDto(productVariant);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVariantResponseDto getProductVariantById(Long id) {
        ProductVariant productVariant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant not found with id: " + id));
        return productVariantMapper.toDto(productVariant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantResponseDto> getProductVariantsByProductId(Long productId) {
        productRepository.findById(productId) // Kiểm tra product tồn tại
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return productVariantRepository.findByProductId(productId).stream()
                .map(productVariantMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantResponseDto> getAllProductVariants() {
        return productVariantRepository.findAll().stream()
                .map(productVariantMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductVariantResponseDto updateProductVariant(Long id, ProductVariantRequestDto requestDto) {
        ProductVariant existingProductVariant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant not found with id: " + id));

        // Kiểm tra Product tồn tại nếu productId trong requestDto khác với product hiện tại của variant
        if (!existingProductVariant.getProduct().getId().equals(requestDto.getProductId())) {
            productRepository.findById(requestDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + requestDto.getProductId()));
        }

        // Kiểm tra trùng lặp size và color cho cùng một sản phẩm, ngoại trừ chính variant đang cập nhật
        productVariantRepository.findByProductIdAndSizeAndColor(
                requestDto.getProductId(), requestDto.getSize(), requestDto.getColor())
                .filter(pv -> !pv.getId().equals(id))
                .ifPresent(pv -> {
                    throw new IllegalArgumentException("Product Variant with size '" + requestDto.getSize() + "' and color '" + requestDto.getColor() + "' already exists for product id: " + requestDto.getProductId());
                });

        existingProductVariant.setSize(requestDto.getSize());
        existingProductVariant.setColor(requestDto.getColor());
        existingProductVariant.setStock(requestDto.getStock());

        // Cập nhật product nếu có thay đổi (mặc dù thường không nên thay đổi product của variant)
        if (!existingProductVariant.getProduct().getId().equals(requestDto.getProductId())) {
            Product newProduct = new Product();
            newProduct.setId(requestDto.getProductId());
            existingProductVariant.setProduct(newProduct);
        }

        existingProductVariant = productVariantRepository.save(existingProductVariant);
        return productVariantMapper.toDto(existingProductVariant);
    }

    @Override
    @Transactional
    public void deleteProductVariant(Long id) {
        if (!productVariantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product Variant not found with id: " + id);
        }
        productVariantRepository.deleteById(id);
    }
}
