package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.UserRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.UserUpdateRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.UserResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Role;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.User;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.VerificationToken;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.RoleName;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.UserStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.UserMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.UserRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.VerificationTokenRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.EmailService;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.RoleService;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public UserResponseDto createUser(UserRequestDto requestDto) {
        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = userMapper.toEntity(requestDto);
        user.setPasswordHash(passwordEncoder.encode(requestDto.getPassword()));
        user.setRole(roleService.getRoleEntityByName(RoleName.CUSTOMER));

        // Đặt trạng thái là PENDING thay vì ACTIVE
        user.setStatus(UserStatus.PENDING);

        user = userRepository.save(user);

        // Tạo mã OTP 6 số
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));

        // Lưu vào bảng token
        VerificationToken token = VerificationToken.builder()
                .email(user.getEmail())
                .otpCode(otp)
                .expiryDate(LocalDateTime.now().plusMinutes(5)) // Hết hạn sau 5 phút
                .build();
        tokenRepository.save(token);

        // Gửi mail
        emailService.sendOtpEmail(user.getEmail(), otp);

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
    public UserResponseDto updateUser(Long id, UserUpdateRequestDto requestDto) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + id));

        existingUser.setFullName(requestDto.getFullName());
        existingUser.setPhone(requestDto.getPhone());
        existingUser.setGender(requestDto.getGender());
        existingUser.setDateOfBirth(requestDto.getDateOfBirth());
        existingUser.setAvatar(requestDto.getAvatar());

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
    @Override
    @Transactional
    public boolean verifyOtp(String email, String otp) {
        VerificationToken token = tokenRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã xác thực cho email này"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(token); // Xóa token đã hết hạn
            throw new IllegalArgumentException("Mã xác thực đã hết hạn");
        }

        if (!token.getOtpCode().equals(otp)) {
            return false;
        }

        // Xác thực thành công -> Cập nhật User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        tokenRepository.delete(token); // Xóa token sau khi dùng
        return true;
    }
}
