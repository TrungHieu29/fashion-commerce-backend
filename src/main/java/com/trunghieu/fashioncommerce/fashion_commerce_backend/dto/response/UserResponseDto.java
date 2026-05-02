package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.UserStatus; // Import UserStatus
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.Gender; // Import Gender enum
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private Long id;
    private String username;
    private String fullName;
    private String phone;
    private UserStatus status; // Đã thay đổi từ Integer sang UserStatus
    private String email;
    private String avatar;
    private Gender gender; // Đã thay đổi từ String sang Gender enum
    private LocalDateTime dateOfBirth;
    private LocalDateTime createdAt;
    private String roleName; // Chỉ lấy tên Role thay vì cả đối tượng Role
}
