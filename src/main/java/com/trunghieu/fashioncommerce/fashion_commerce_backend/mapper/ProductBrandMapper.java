package com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ProductBrandRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductBrandResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductBrand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface ProductBrandMapper {


    @Mapping(target = "id", ignore = true)
    ProductBrand toEntity(ProductBrandRequestDto productBrandRequestDto);

    ProductBrandResponseDto toDto(ProductBrand productBrand);
}
