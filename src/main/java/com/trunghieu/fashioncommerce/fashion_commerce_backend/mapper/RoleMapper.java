package com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Role;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.RoleRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.RoleResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "id", ignore = true)
    Role toEntity(RoleRequestDto dto);

    RoleResponseDto toDto(Role entity);
}
