package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.UserRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.UserUpdateRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.UserResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.UserStatus;

import java.util.List;

public interface UserService {
    String createUser(UserRequestDto requestDto);
    UserResponseDto getUserById(Long id);
    UserResponseDto getUserByUsername(String username);
    List<UserResponseDto> getAllUsers();
    UserResponseDto updateUser(Long id, UserUpdateRequestDto requestDto);
    void deleteUser(Long id);
    boolean verifyOtp(String email, String otp);
    UserResponseDto updateUserStatus(Long id, UserStatus status);
    void resendOtp(String email);
}
