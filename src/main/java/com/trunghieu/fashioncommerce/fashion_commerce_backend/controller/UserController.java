package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ChangePasswordRequest;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.UserRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.UserStatusRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.UserUpdateRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.UserResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import PreAuthorize
import org.springframework.security.core.Authentication; // Import Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id") // Chỉ ADMIN hoặc chính người dùng
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id, Authentication authentication) {
        // authentication.principal sẽ là CustomUserDetails
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.principal.username") // Chỉ ADMIN hoặc chính người dùng
    public ResponseEntity<UserResponseDto> getUserByUsername(@PathVariable String username, Authentication authentication) {
        UserResponseDto user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id") // Chỉ ADMIN hoặc chính người dùng
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequestDto requestDto, Authentication authentication) {
        UserResponseDto updatedUser = userService.updateUser(id, requestDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id") // Chỉ ADMIN hoặc chính người dùng
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication authentication) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> updateUserStatus(
            @PathVariable Long id,
            @RequestBody UserStatusRequestDto requestDto) {

        return ResponseEntity.ok(
                userService.updateUserStatus(
                        id,
                        requestDto.getStatus()
                )
        );
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ChangePasswordRequest request
    ) {

        userService.changePassword(
                userDetails.getUsername(),
                request
        );

        return ResponseEntity.ok(
                "Đổi mật khẩu thành công"
        );
    }
}
