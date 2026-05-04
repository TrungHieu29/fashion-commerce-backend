package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.RoleResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Role;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.RoleName;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.RoleMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.RoleRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.RoleService;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional(readOnly = true)
    public RoleResponseDto getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return roleMapper.toDto(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponseDto getRoleByName(RoleName name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
        return roleMapper.toDto(role);
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleEntityByName(RoleName name) { // Triển khai phương thức mới
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponseDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toDto)
                .collect(Collectors.toList());
    }
}
