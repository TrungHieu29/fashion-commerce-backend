package com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductImage;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ProductImageRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductImageResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    ProductImage toEntity(ProductImageRequestDto dto);

    @Mapping(source = "product.id", target = "productId")
    ProductImageResponseDto toDto(ProductImage entity);
}
