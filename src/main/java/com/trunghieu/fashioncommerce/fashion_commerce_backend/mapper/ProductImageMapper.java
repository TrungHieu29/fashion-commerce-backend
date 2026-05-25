package com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductImage;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ProductImageRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductImageResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Builder;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ProductImageMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    ProductImage toEntity(ProductImageRequestDto dto);

    @Mapping(source = "product.id", target = "productId")
    ProductImageResponseDto toDto(ProductImage entity);
}
