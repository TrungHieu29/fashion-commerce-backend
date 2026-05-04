package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.UserRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.UserResponseDto;
import java.util.List;

public interface UserService {
    UserResponseDto createUser(UserRequestDto requestDto);
    UserResponseDto getUserById(Long id);
    UserResponseDto getUserByUsername(String username);
    List<UserResponseDto> getAllUsers();
    UserResponseDto updateUser(Long id, UserRequestDto requestDto);
    void deleteUser(Long id);
}
