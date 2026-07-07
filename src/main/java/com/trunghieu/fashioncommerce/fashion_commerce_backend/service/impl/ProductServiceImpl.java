package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ProductRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Category;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Discount;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Product;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductBrand;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductImage;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductVariant;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Shop;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.DiscountStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.DiscountTarget;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.DiscountType;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.ProductMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.CategoryRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.DiscountRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductImageRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ProductVariantRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.CategoryService;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ProductBrandService;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ProductService;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ShopService shopService;
    private final CategoryService categoryService;
    private final ProductBrandService productBrandService;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final DiscountRepository discountRepository;

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
        return enrichProductPage(productRepository.findAllActive(pageable), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsByShopId(Long shopId, Pageable pageable) {
        shopService.getShopById(shopId);
        return enrichProductPage(productRepository.findByShopId(shopId, pageable), pageable);
    }

    private ProductResponseDto enrichProductDto(Product product) {
        return enrichProducts(List.of(product)).get(0);
    }

    private Page<ProductResponseDto> enrichProductPage(Page<Product> productPage, Pageable pageable) {
        List<ProductResponseDto> content = enrichProducts(productPage.getContent());
        return new PageImpl<>(content, pageable, productPage.getTotalElements());
    }

    private List<ProductResponseDto> enrichProducts(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> productIds = products.stream()
                .map(Product::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> shopIds = products.stream()
                .map(Product::getShop)
                .filter(Objects::nonNull)
                .map(Shop::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> thumbnailsByProductId = resolveProductThumbnails(productIds);
        Map<Long, List<Discount>> activeDiscountsByShopId = loadActiveDiscountsByShopId(shopIds);

        return products.stream()
                .map(product -> {
                    ProductResponseDto dto = productMapper.toDto(product);
                    BigDecimal originalPrice = product.getPrice();
                    BigDecimal discountAmount = calculateBestDiscount(
                            activeDiscountsByShopId.getOrDefault(product.getShop().getId(), Collections.emptyList()),
                            product.getId(),
                            originalPrice
                    );

                    dto.setOriginalPrice(originalPrice);
                    dto.setDiscountAmount(discountAmount);
                    dto.setFinalPrice(originalPrice == null ? null : originalPrice.subtract(discountAmount));
                    dto.setImageUrl(thumbnailsByProductId.get(product.getId()));
                    return dto;
                })
                .toList();
    }

    private Map<Long, List<Discount>> loadActiveDiscountsByShopId(Set<Long> shopIds) {
        if (shopIds == null || shopIds.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDateTime now = LocalDateTime.now();
        return discountRepository
                .findActiveByShopIdsWithProducts(shopIds, DiscountStatus.ACTIVE, now)
                .stream()
                .collect(Collectors.groupingBy(discount -> discount.getShop().getId()));
    }

    private Map<Long, String> resolveProductThumbnails(Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, List<ProductImage>> imagesByProductId = productImageRepository
                .findByProductIdInOrderByProductIdAscCreatedAtAscIdAsc(productIds)
                .stream()
                .collect(Collectors.groupingBy(image -> image.getProduct().getId()));

        Map<Long, List<String>> availableColorsByProductId = productVariantRepository
                .findByProductIdInAndStockGreaterThanOrderByProductIdAscIdAsc(productIds, 0)
                .stream()
                .collect(Collectors.groupingBy(
                        variant -> variant.getProduct().getId(),
                        Collectors.mapping(ProductVariant::getColor, Collectors.toList())
                ));

        Map<Long, String> thumbnailsByProductId = new HashMap<>();
        productIds.forEach(productId -> thumbnailsByProductId.put(
                productId,
                resolveProductThumbnail(
                        imagesByProductId.getOrDefault(productId, Collections.emptyList()),
                        availableColorsByProductId.getOrDefault(productId, Collections.emptyList())
                )
        ));
        return thumbnailsByProductId;
    }

    private String resolveProductThumbnail(List<ProductImage> images, List<String> availableColors) {
        if (images == null || images.isEmpty()) {
            return null;
        }

        String matchingColorImageUrl = availableColors.stream()
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

    private BigDecimal calculateBestDiscount(List<Discount> activeDiscounts, Long productId, BigDecimal originalPrice) {
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0 || activeDiscounts.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal maxDiscountAmount = BigDecimal.ZERO;
        for (Discount discount : activeDiscounts) {
            boolean canApply = discount.getDiscountTarget() == DiscountTarget.SHOP;
            if (discount.getDiscountTarget() == DiscountTarget.PRODUCT && discount.getProducts() != null) {
                canApply = discount.getProducts().stream().anyMatch(product -> product.getId().equals(productId));
            }

            if (canApply) {
                BigDecimal currentAmount = calculateDiscountAmount(discount, originalPrice);
                if (currentAmount.compareTo(maxDiscountAmount) > 0) {
                    maxDiscountAmount = currentAmount;
                }
            }
        }

        return maxDiscountAmount;
    }

    private BigDecimal calculateDiscountAmount(Discount discount, BigDecimal price) {
        BigDecimal discountAmount;
        if (discount.getDiscountType() == DiscountType.PERCENT) {
            discountAmount = price.multiply(discount.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discountAmount = discount.getDiscountValue();
        }

        return discountAmount.min(price);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsByCategoryId(Long categoryId, Pageable pageable) {
        categoryService.getCategoryById(categoryId);
        return enrichProductPage(productRepository.findByCategoryId(categoryId, pageable), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsByBrandId(Long brandId, Pageable pageable) {
        productBrandService.getProductBrandById(brandId);
        return enrichProductPage(productRepository.findByBrandId(brandId, pageable), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> searchProducts(String keyword, Pageable pageable) {
        return enrichProductPage(productRepository.findByProductNameContainingIgnoreCase(keyword, pageable), pageable);
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
