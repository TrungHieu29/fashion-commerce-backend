package com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Order;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.OrderStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", uses = {OrderShopMapper.class, PaymentMapper.class}, imports = OrderStatus.class) // Đã thêm imports = OrderStatus.class
public interface OrderMapper {
    @Mapping(target = "id", ignore = true) // Corrected from orderId to id
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "finalPrice", ignore = true)
    @Mapping(target = "status", expression = "java(OrderStatus.PENDING)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderShops", ignore = true)
    @Mapping(target = "payment", ignore = true)
    Order toEntity(OrderRequestDto orderRequestDto);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userFullName")
    OrderResponseDto toDto(Order order);
}
