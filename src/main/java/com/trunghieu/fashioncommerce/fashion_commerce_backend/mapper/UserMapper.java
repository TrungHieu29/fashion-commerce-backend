package com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.UserRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.UserResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.User;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.UserStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", imports = UserStatus.class) // Đã thêm imports = UserStatus.class
public interface UserMapper {

    @Mapping(target = "passwordHash", ignore = true) // Bỏ qua passwordHash khi map từ request
    @Mapping(target = "id", ignore = true) // ID sẽ được sinh ra bởi DB
    @Mapping(target = "createdAt", ignore = true) // createdAt sẽ được @PrePersist xử lý
    @Mapping(target = "status", expression = "java(UserStatus.ACTIVE)") // Mặc định status là ACTIVE khi tạo mới
    @Mapping(target = "role", ignore = true) // Role sẽ được set trong service
    @Mapping(target = "shippingAddresses", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "shop", ignore = true)
    User toEntity(UserRequestDto userRequestDto);

    @Mapping(source = "role.name", target = "roleName") // Map role.name sang roleName trong DTO
    UserResponseDto toDto(User user);
}
