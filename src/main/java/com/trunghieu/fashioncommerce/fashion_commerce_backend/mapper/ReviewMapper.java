package com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ReviewRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ReviewResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface ReviewMapper {



    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true) // User sẽ được set trong service
    @Mapping(target = "product", ignore = true) // Product sẽ được set trong service
    @Mapping(target = "orderItem", ignore = true) // OrderItem sẽ được set trong service
    Review toEntity(ReviewRequestDto reviewRequestDto);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.productName", target = "productName")
    @Mapping(source = "orderItem.id", target = "orderItemId")
    ReviewResponseDto toDto(Review review);
}
