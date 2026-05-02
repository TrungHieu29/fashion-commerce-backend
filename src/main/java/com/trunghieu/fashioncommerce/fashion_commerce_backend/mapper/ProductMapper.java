package com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ProductRequestDto;
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
    @Mapping(target = "rating", ignore = true) // Rating sẽ được tính toán, không set từ request
    @Mapping(target = "shop", ignore = true) // Shop sẽ được set trong service
    @Mapping(target = "brand", ignore = true) // Brand sẽ được set trong service
    @Mapping(target = "category", ignore = true) // Category sẽ được set trong service
    @Mapping(target = "variants", ignore = true) // Variants sẽ được quản lý riêng
    @Mapping(target = "images", ignore = true) // Images sẽ được quản lý riêng
    @Mapping(target = "discounts", ignore = true) // Discounts sẽ được quản lý riêng
    Product toEntity(ProductRequestDto productRequestDto);

    @Mapping(source = "shop.id", target = "shopId")
    @Mapping(source = "shop.shopName", target = "shopName")
    @Mapping(source = "brand.id", target = "brandId")
    @Mapping(source = "brand.name", target = "brandName")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ProductResponseDto toDto(Product product);
}
