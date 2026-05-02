package com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.CartItemRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.CartItemResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.CartItem;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "productVariant", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    CartItem toEntity(CartItemRequestDto dto);

    @Mapping(source = "productVariant.id", target = "productVariantId")
    @Mapping(source = "productVariant.product.productName", target = "productName")
    @Mapping(source = "productVariant.size", target = "size")
    @Mapping(source = "productVariant.color", target = "color")
    @Mapping(source = "productVariant.product.price", target = "price")
    @Mapping(source = "productVariant.product.images", target = "imageUrl", qualifiedByName = "mapProductImages")
    CartItemResponseDto toDto(CartItem entity);

    @Named("mapProductImages")
    default String mapProductImages(Set<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.iterator().next().getImageUrl();
    }
}
