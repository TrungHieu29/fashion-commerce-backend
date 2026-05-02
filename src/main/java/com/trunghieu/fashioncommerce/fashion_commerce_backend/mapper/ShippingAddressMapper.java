package com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.ShippingAddress;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ShippingAddressRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ShippingAddressResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShippingAddressMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    ShippingAddress toEntity(ShippingAddressRequestDto dto);

    @Mapping(source = "user.id", target = "userId")
    ShippingAddressResponseDto toDto(ShippingAddress entity);
}
