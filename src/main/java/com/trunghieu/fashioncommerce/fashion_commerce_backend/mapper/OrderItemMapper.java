package com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderItemRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderItemResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderShop", ignore = true)
    @Mapping(target = "productVariant", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "review", ignore = true)
    OrderItem toEntity(OrderItemRequestDto orderItemRequestDto);

    @Mapping(source = "productVariant.id", target = "productVariantId")
    @Mapping(source = "productVariant.product.productName", target = "productName")
    @Mapping(source = "productVariant.size", target = "size")
    @Mapping(source = "productVariant.color", target = "color")
    OrderItemResponseDto toDto(OrderItem orderItem);
}
