package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.Gender; // Import Gender enum
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDto {
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password; // Client gửi password thô

    @NotBlank(message = "Full name cannot be blank")
    private String fullName;

    @Email(message = "Invalid email format")
    private String email;

    private String phone;
    private Gender gender; // Đã thay đổi từ String sang Gender enum
    private LocalDateTime dateOfBirth;
    private String avatar;
}
