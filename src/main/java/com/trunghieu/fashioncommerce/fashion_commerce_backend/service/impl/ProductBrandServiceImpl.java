package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ProductBrandRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductBrandResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductBrand;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.ProductBrandMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductBrandRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ProductBrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductBrandServiceImpl implements ProductBrandService {

    private final ProductBrandRepository productBrandRepository;
    private final ProductBrandMapper productBrandMapper;

    @Override
    @Transactional
    public ProductBrandResponseDto createProductBrand(ProductBrandRequestDto requestDto) {
        if (productBrandRepository.findByName(requestDto.getName()).isPresent()) {
            throw new IllegalArgumentException("Product brand already exists with name: " + requestDto.getName());
        }
        ProductBrand productBrand = productBrandMapper.toEntity(requestDto);
        ProductBrand savedProductBrand = productBrandRepository.save(productBrand);
        return productBrandMapper.toDto(savedProductBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductBrandResponseDto getProductBrandById(Long id) {
        ProductBrand productBrand = productBrandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product brand not found with id: " + id));
        return productBrandMapper.toDto(productBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductBrandResponseDto> getAllProductBrands() {
        return productBrandRepository.findAll().stream()
                .map(productBrandMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductBrandResponseDto updateProductBrand(Long id, ProductBrandRequestDto requestDto) {
        ProductBrand existingProductBrand = productBrandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product brand not found with id: " + id));

        if (!existingProductBrand.getName().equals(requestDto.getName()) &&
                productBrandRepository.findByName(requestDto.getName()).isPresent()) {
            throw new IllegalArgumentException("Product brand already exists with name: " + requestDto.getName());
        }

        existingProductBrand.setName(requestDto.getName());
        existingProductBrand.setDescription(requestDto.getDescription());

        ProductBrand updatedProductBrand = productBrandRepository.save(existingProductBrand);
        return productBrandMapper.toDto(updatedProductBrand);
    }


    @Override
    @Transactional
    public void deleteProductBrand(Long id) {
        if (!productBrandRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product brand not found with id: " + id);
        }
        productBrandRepository.deleteById(id);
    }
}