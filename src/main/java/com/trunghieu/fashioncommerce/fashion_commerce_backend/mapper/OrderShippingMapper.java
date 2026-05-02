package com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderShippingRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderShippingResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderShipping;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.ShippingStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", imports = ShippingStatus.class) // Đã thêm imports = ShippingStatus.class
public interface OrderShippingMapper {



    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderShop", ignore = true) // OrderShop sẽ được set trong service
    OrderShipping toEntity(OrderShippingRequestDto orderShippingRequestDto);

    OrderShippingResponseDto toDto(OrderShipping orderShipping);
}
