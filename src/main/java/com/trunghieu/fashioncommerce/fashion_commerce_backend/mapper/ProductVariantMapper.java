package com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductVariant;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ProductVariantRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductVariantResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    ProductVariant toEntity(ProductVariantRequestDto dto);

    @Mapping(source = "product.id", target = "productId")
    ProductVariantResponseDto toDto(ProductVariant entity);
}
