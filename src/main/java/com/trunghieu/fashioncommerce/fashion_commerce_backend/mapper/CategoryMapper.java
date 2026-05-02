package com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.CategoryRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.CategoryResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface CategoryMapper {



    @Mapping(target = "id", ignore = true)
    Category toEntity(CategoryRequestDto categoryRequestDto);

    CategoryResponseDto toDto(Category category);
}
