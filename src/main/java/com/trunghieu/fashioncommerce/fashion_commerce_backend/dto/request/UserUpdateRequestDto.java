package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequestDto {
    @NotBlank(message = "Full name cannot be blank")
    private String fullName;

    @Pattern(
            regexp = "^(0|\\+84)[0-9]{9}$",
            message = "Invalid phone number format"
    )
    private String phone;

    private Gender gender; // Đã thay đổi từ String sang Gender enum
    private LocalDate dateOfBirth;
    private String avatar;
}
