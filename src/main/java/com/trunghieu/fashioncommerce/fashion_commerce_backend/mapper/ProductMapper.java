package com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ProductRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.CategoryResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ProductResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Product;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Shop;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ProductBrand;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "categories", ignore = true) // Đổi từ "category" sang "categories"
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "discounts", ignore = true)
    Product toEntity(ProductRequestDto productRequestDto);

    @Mapping(source = "shop.id", target = "shopId")
    @Mapping(source = "shop.shopName", target = "shopName")
    @Mapping(source = "brand.id", target = "brandId")
    @Mapping(source = "brand.name", target = "brandName")
    // MapStruct sẽ tự động map Set<Category> sang Set<CategoryResponseDto>
    // nếu bạn định nghĩa hàm chuyển đổi cho Category (hoặc để mặc định)
    @Mapping(source = "categories", target = "categories")
    ProductResponseDto toDto(Product product);

    // Thêm hàm ánh xạ cho danh mục để MapStruct hiểu cách chuyển từ Entity -> DTO
    CategoryResponseDto categoryToCategoryResponseDto(Category category);
}
