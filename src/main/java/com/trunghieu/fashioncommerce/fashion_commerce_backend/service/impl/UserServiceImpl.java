package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.UserRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.UserResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Role;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.User;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.RoleName;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.UserMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.UserRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.RoleService;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDto createUser(UserRequestDto requestDto) {
        // Kiểm tra username và email đã tồn tại chưa
        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + requestDto.getUsername());
        }
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + requestDto.getEmail());
        }

        User user = userMapper.toEntity(requestDto);

        // Mã hóa mật khẩu
        user.setPasswordHash(passwordEncoder.encode(requestDto.getPassword()));

        // Gán vai trò mặc định là CUSTOMER
        // Đã sửa: Lấy Role entity trực tiếp từ RoleService
        Role customerRole = roleService.getRoleEntityByName(RoleName.CUSTOMER);
        user.setRole(customerRole);

        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto requestDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Cập nhật các trường từ requestDto vào existingUser
        existingUser.setFullName(requestDto.getFullName());
        existingUser.setEmail(requestDto.getEmail());
        existingUser.setPhone(requestDto.getPhone());
        existingUser.setGender(requestDto.getGender());
        existingUser.setDateOfBirth(requestDto.getDateOfBirth());
        existingUser.setAvatar(requestDto.getAvatar());

        // Nếu có mật khẩu mới trong request, mã hóa và cập nhật
        if (requestDto.getPassword() != null && !requestDto.getPassword().isEmpty()) {
            existingUser.setPasswordHash(passwordEncoder.encode(requestDto.getPassword()));
        }

        userRepository.save(existingUser);
        return userMapper.toDto(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
