package com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.OrderShopRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.OrderShopResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.OrderShop;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", uses = {OrderItemMapper.class, OrderShippingMapper.class})
public interface OrderShopMapper {



    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true) // Order sẽ được set trong service
    @Mapping(target = "shop", ignore = true) // Shop sẽ được set trong service
    @Mapping(target = "totalPrice", ignore = true) // Tính toán trong service
    @Mapping(target = "finalPrice", ignore = true) // Tính toán trong service
    @Mapping(target = "discount", ignore = true) // Discount sẽ được set trong service
    @Mapping(target = "orderItems", ignore = true) // OrderItems sẽ được xử lý riêng
    @Mapping(target = "shipping", ignore = true) // Shipping sẽ được xử lý riêng
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "stockDeducted", ignore = true) // Thêm ignore cho trường mới
    OrderShop toEntity(OrderShopRequestDto orderShopRequestDto);

    @Mapping(source = "order.id", target = "orderId") // Corrected from order.orderId to order.id
    @Mapping(source = "shop.id", target = "shopId")
    @Mapping(source = "shop.shopName", target = "shopName")
    @Mapping(source = "discount.id", target = "discountId")
    @Mapping(source = "stockDeducted", target = "stockDeducted") // Thêm ánh xạ cho stockDeducted
    OrderShopResponseDto toDto(OrderShop orderShop);
}
