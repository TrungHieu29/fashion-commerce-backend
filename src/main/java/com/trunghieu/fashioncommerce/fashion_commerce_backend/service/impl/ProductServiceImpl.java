package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ProductRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Product;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Shop;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductBrand;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Category;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.ProductMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.CategoryRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ShopRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductBrandRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;
    private final ProductBrandRepository productBrandRepository;

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto) {
        Product product = productMapper.toEntity(requestDto);

        // Set relationships
        if (requestDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(requestDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + requestDto.getCategoryId()));
            product.setCategory(category);
        }

        if (requestDto.getShopId() != null) {
            Shop shop = shopRepository.findById(requestDto.getShopId())
                    .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + requestDto.getShopId()));
            product.setShop(shop);
        }

        if (requestDto.getBrandId() != null) {
            ProductBrand brand = productBrandRepository.findById(requestDto.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product brand not found with id: " + requestDto.getBrandId()));
            product.setBrand(brand);
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productMapper.toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProductsByCategory(Long categoryId) {
        Pageable pageable = Pageable.unpaged(); // Get all for now
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
        return products.stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProductsByShop(Long shopId) {
        Pageable pageable = Pageable.unpaged(); // Get all for now
        Page<Product> products = productRepository.findByShopId(shopId, pageable);
        return products.stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProductsByBrand(Long brandId) {
        Pageable pageable = Pageable.unpaged(); // Get all for now
        Page<Product> products = productRepository.findByBrandId(brandId, pageable);
        return products.stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductRequestDto requestDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Update fields from requestDto
        existingProduct.setProductName(requestDto.getProductName());
        existingProduct.setProductDetail(requestDto.getProductDetail());
        existingProduct.setStatus(requestDto.getStatus());
        existingProduct.setPrice(requestDto.getPrice());

        // Update relationships
        if (requestDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(requestDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + requestDto.getCategoryId()));
            existingProduct.setCategory(category);
        }

        if (requestDto.getShopId() != null) {
            Shop shop = shopRepository.findById(requestDto.getShopId())
                    .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + requestDto.getShopId()));
            existingProduct.setShop(shop);
        }

        if (requestDto.getBrandId() != null) {
            ProductBrand brand = productBrandRepository.findById(requestDto.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product brand not found with id: " + requestDto.getBrandId()));
            existingProduct.setBrand(brand);
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toDto(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}