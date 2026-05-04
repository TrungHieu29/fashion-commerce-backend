package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.RoleResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Role; // Import Role entity
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.RoleName;

import java.util.List;

public interface RoleService {
    RoleResponseDto getRoleById(Long id);
    RoleResponseDto getRoleByName(RoleName name);
    Role getRoleEntityByName(RoleName name); // Thêm phương thức này để trả về Role entity
    List<RoleResponseDto> getAllRoles();
}
