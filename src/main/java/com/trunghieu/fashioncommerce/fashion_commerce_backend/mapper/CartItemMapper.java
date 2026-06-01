package com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.CartItemRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.CartItemResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.CartItem;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductImage;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.DiscountService; // Import DiscountService
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired; // Import Autowired

import java.math.BigDecimal;
import java.util.Set;

@Mapper(componentModel = "spring")
public abstract class CartItemMapper { // Thay đổi thành abstract class để inject service

    @Autowired
    protected DiscountService discountService; // Inject DiscountService

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "productVariant", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    public abstract CartItem toEntity(CartItemRequestDto dto);

    @Mapping(source = "productVariant.id", target = "productVariantId")
    @Mapping(source = "productVariant.product.productName", target = "productName")
    @Mapping(source = "productVariant.size", target = "size")
    @Mapping(source = "productVariant.color", target = "color")
    @Mapping(source = "productVariant.product.id", target = "productId")
    @Mapping(source = "productVariant.product.shop.id", target = "shopId")
    @Mapping(source = "productVariant.product.shop.shopName", target = "shopName")

    @Mapping(target = "imageUrl", expression = "java(mapVariantImage(entity))")

    // Cập nhật mapping cho price để tính giá đã giảm
    @Mapping(target = "price", expression = "java(calculateFinalPrice(entity))")

    // Cập nhật mapping cho subtotal để sử dụng giá đã giảm
    @Mapping(
            target = "subtotal",
            expression = "java(calculateFinalPrice(entity).multiply(java.math.BigDecimal.valueOf(entity.getQuantity())))"
    )
    public abstract CartItemResponseDto toDto(CartItem entity);

    /**
     * Phương thức helper để tính giá cuối cùng của một sản phẩm sau khi áp dụng giảm giá tự động.
     */
    protected BigDecimal calculateFinalPrice(CartItem cartItem) {
        BigDecimal originalPrice = cartItem.getProductVariant().getProduct().getPrice();
        Long shopId = cartItem.getProductVariant().getProduct().getShop().getId();
        Long productId = cartItem.getProductVariant().getProduct().getId();

        BigDecimal discountAmount = discountService.calculateBestDiscount(shopId, productId, originalPrice);
        return originalPrice.subtract(discountAmount);
    }


    /**
     * Logic lọc ảnh theo màu sắc của biến thể.
     * Nếu tìm thấy ảnh khớp màu sẽ ưu tiên hiển thị, nếu không sẽ lấy ảnh đầu tiên của sản phẩm.
     */
    protected String mapVariantImage(CartItem entity) {
        if (entity == null || entity.getProductVariant() == null ||
                entity.getProductVariant().getProduct() == null ||
                entity.getProductVariant().getProduct().getImages() == null) {
            return null;
        }

        String variantColor = entity.getProductVariant().getColor();
        Set<ProductImage> images = entity.getProductVariant().getProduct().getImages();

        if (images == null || images.isEmpty()) {
            return null;
        }

        // Tìm ảnh có màu khớp với màu của variant (không phân biệt hoa thường)
        return images.stream()
                .filter(img -> variantColor != null && variantColor.equalsIgnoreCase(img.getColor()))
                .map(ProductImage::getImageUrl)
                .findFirst()
                // Nếu không tìm thấy ảnh khớp màu, fallback lấy tấm ảnh đầu tiên của sản phẩm làm mặc định
                .orElseGet(() -> images.iterator().next().getImageUrl());
    }
}
