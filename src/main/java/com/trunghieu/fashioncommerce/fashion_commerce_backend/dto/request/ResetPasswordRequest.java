package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @Email
    private String email;

    private String otp;

    @NotBlank
    @Size(min = 6)
    private String newPassword;

    @NotBlank
    private String confirmPassword;
}
