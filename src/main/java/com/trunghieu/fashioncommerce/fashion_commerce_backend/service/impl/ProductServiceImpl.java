package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ProductRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Category;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Product;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductBrand;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductImage;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductVariant;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Shop;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.ProductMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.CategoryRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductImageRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductVariantRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.CategoryService;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.DiscountService;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ProductBrandService;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ProductService;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ShopService shopService; // Để kiểm tra shop tồn tại
    private final CategoryService categoryService; // Để kiểm tra category tồn tại
    private final ProductBrandService productBrandService; // Để kiểm tra brand tồn tại
    private final DiscountService discountService;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto) {

        shopService.getShopById(requestDto.getShopId());
        productBrandService.getProductBrandById(requestDto.getBrandId());

        Set<Category> categories = requestDto.getCategoryIds()
                .stream()
                .map(id -> categoryRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Category not found with id: " + id)))
                .collect(Collectors.toSet());

        Product product = productMapper.toEntity(requestDto);

        Shop shop = new Shop();
        shop.setId(requestDto.getShopId());
        product.setShop(shop);

        ProductBrand brand = new ProductBrand();
        brand.setId(requestDto.getBrandId());
        product.setBrand(brand);

        product.setCategories(categories);

        product = productRepository.save(product);

        return enrichProductDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return enrichProductDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
        return productRepository.findAllActive(pageable)
                .map(this::enrichProductDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsByShopId(Long shopId, Pageable pageable) {
        shopService.getShopById(shopId); // Kiểm tra shop tồn tại
        return productRepository.findByShopId(shopId, pageable).map(this::enrichProductDto);
    }

    private ProductResponseDto enrichProductDto(Product product) {
        ProductResponseDto dto = productMapper.toDto(product);
        BigDecimal originalPrice = product.getPrice();
        BigDecimal discountAmount = discountService.calculateBestDiscount(product.getShop().getId(), product.getId(),
                originalPrice);

        dto.setOriginalPrice(originalPrice);
        dto.setDiscountAmount(discountAmount);
        dto.setFinalPrice(originalPrice.subtract(discountAmount));
        dto.setImageUrl(resolveProductThumbnail(product.getId()));
        return dto;
    }

    private String resolveProductThumbnail(Long productId) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderByCreatedAtAscIdAsc(productId);
        if (images.isEmpty()) {
            return null;
        }

        String matchingColorImageUrl = productVariantRepository
                .findByProductIdAndStockGreaterThanOrderByIdAsc(productId, 0)
                .stream()
                .map(ProductVariant::getColor)
                .filter(color -> color != null && !color.isBlank())
                .distinct()
                .flatMap(color -> images.stream()
                        .filter(image -> image.getColor() != null && color.equalsIgnoreCase(image.getColor()))
                        .map(ProductImage::getImageUrl))
                .filter(imageUrl -> imageUrl != null && !imageUrl.isBlank())
                .findFirst()
                .orElse(null);

        if (matchingColorImageUrl != null) {
            return matchingColorImageUrl;
        }

        return images.stream()
                .map(ProductImage::getImageUrl)
                .filter(imageUrl -> imageUrl != null && !imageUrl.isBlank())
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsByCategoryId(Long categoryId, Pageable pageable) {
        categoryService.getCategoryById(categoryId); // Kiểm tra category tồn tại
        return productRepository.findByCategoryId(categoryId, pageable).map(this::enrichProductDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsByBrandId(Long brandId, Pageable pageable) {
        productBrandService.getProductBrandById(brandId); // Kiểm tra brand tồn tại
        return productRepository.findByBrandId(brandId, pageable).map(this::enrichProductDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByProductNameContainingIgnoreCase(keyword, pageable).map(this::enrichProductDto);
    }

    @Override
    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductRequestDto requestDto) {

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));

        if (!existingProduct.getShop().getId().equals(requestDto.getShopId())) {
            shopService.getShopById(requestDto.getShopId());
        }

        if (!existingProduct.getBrand().getId().equals(requestDto.getBrandId())) {
            productBrandService.getProductBrandById(requestDto.getBrandId());
        }

        Set<Category> categories = requestDto.getCategoryIds()
                .stream()
                .map(categoryId -> categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Category not found with id: " + categoryId)))
                .collect(Collectors.toSet());

        existingProduct.setProductName(requestDto.getProductName());
        existingProduct.setProductDetail(requestDto.getProductDetail());
        existingProduct.setStatus(requestDto.getStatus());
        existingProduct.setPrice(requestDto.getPrice());

        Shop shop = new Shop();
        shop.setId(requestDto.getShopId());
        existingProduct.setShop(shop);

        ProductBrand brand = new ProductBrand();
        brand.setId(requestDto.getBrandId());
        existingProduct.setBrand(brand);

        existingProduct.setCategories(categories);

        existingProduct = productRepository.save(existingProduct);

        return enrichProductDto(existingProduct);
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
