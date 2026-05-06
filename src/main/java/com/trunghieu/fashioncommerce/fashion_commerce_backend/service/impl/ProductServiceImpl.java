package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ProductRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Category;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Product;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductBrand;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Shop;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.ProductMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.CategoryService;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ProductBrandService;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ProductService;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ShopService shopService; // Để kiểm tra shop tồn tại
    private final CategoryService categoryService; // Để kiểm tra category tồn tại
    private final ProductBrandService productBrandService; // Để kiểm tra brand tồn tại

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto) {
        // Kiểm tra sự tồn tại của Shop, Category, Brand
        shopService.getShopById(requestDto.getShopId()); // Sẽ ném ResourceNotFoundException nếu không tìm thấy
        categoryService.getCategoryById(requestDto.getCategoryId()); // Sẽ ném ResourceNotFoundException nếu không tìm thấy
        productBrandService.getProductBrandById(requestDto.getBrandId()); // Sẽ ném ResourceNotFoundException nếu không tìm thấy

        Product product = productMapper.toEntity(requestDto);

        // Gán các đối tượng Entity liên quan
        Shop shop = new Shop(); // Tạo đối tượng Shop tạm thời để set ID
        shop.setId(requestDto.getShopId());
        product.setShop(shop);

        Category category = new Category(); // Tạo đối tượng Category tạm thời để set ID
        category.setId(requestDto.getCategoryId());
        product.setCategory(category);

        ProductBrand brand = new ProductBrand(); // Tạo đối tượng ProductBrand tạm thời để set ID
        brand.setId(requestDto.getBrandId());
        product.setBrand(brand);

        product = productRepository.save(product);
        return productMapper.toDto(product);
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
    public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsByShopId(Long shopId, Pageable pageable) {
        shopService.getShopById(shopId); // Kiểm tra shop tồn tại
        return productRepository.findByShopId(shopId, pageable).map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsByCategoryId(Long categoryId, Pageable pageable) {
        categoryService.getCategoryById(categoryId); // Kiểm tra category tồn tại
        return productRepository.findByCategoryId(categoryId, pageable).map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsByBrandId(Long brandId, Pageable pageable) {
        productBrandService.getProductBrandById(brandId); // Kiểm tra brand tồn tại
        return productRepository.findByBrandId(brandId, pageable).map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByProductNameContainingIgnoreCase(keyword, pageable).map(productMapper::toDto);
    }

    @Override
    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductRequestDto requestDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Kiểm tra sự tồn tại của Shop, Category, Brand nếu chúng được thay đổi
        if (!existingProduct.getShop().getId().equals(requestDto.getShopId())) {
            shopService.getShopById(requestDto.getShopId());
        }
        if (!existingProduct.getCategory().getId().equals(requestDto.getCategoryId())) {
            categoryService.getCategoryById(requestDto.getCategoryId());
        }
        if (!existingProduct.getBrand().getId().equals(requestDto.getBrandId())) {
            productBrandService.getProductBrandById(requestDto.getBrandId());
        }

        // Cập nhật các trường
        existingProduct.setProductName(requestDto.getProductName());
        existingProduct.setProductDetail(requestDto.getProductDetail());
        existingProduct.setStatus(requestDto.getStatus());
        existingProduct.setPrice(requestDto.getPrice());

        // Cập nhật các mối quan hệ
        Shop shop = new Shop();
        shop.setId(requestDto.getShopId());
        existingProduct.setShop(shop);

        Category category = new Category();
        category.setId(requestDto.getCategoryId());
        existingProduct.setCategory(category);

        ProductBrand brand = new ProductBrand();
        brand.setId(requestDto.getBrandId());
        existingProduct.setBrand(brand);

        existingProduct = productRepository.save(existingProduct);
        return productMapper.toDto(existingProduct);
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
