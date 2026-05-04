package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.RoleResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.RoleName;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDto> getRoleById(@PathVariable Long id) {
        RoleResponseDto role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    @GetMapping("/name/{name}") // Endpoint mới để lấy Role theo tên
    public ResponseEntity<RoleResponseDto> getRoleByName(@PathVariable RoleName name) {
        RoleResponseDto role = roleService.getRoleByName(name);
        return ResponseEntity.ok(role);
    }

    @GetMapping
    public ResponseEntity<List<RoleResponseDto>> getAllRoles() {
        List<RoleResponseDto> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
}
