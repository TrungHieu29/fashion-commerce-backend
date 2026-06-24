package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ChangePasswordRequest;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ResetPasswordRequest;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.UserRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.UserUpdateRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.UserResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.PasswordResetToken;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.PendingRegistration;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.User;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.RoleName;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.UserStatus;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper.UserMapper;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.PasswordResetTokenRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.PendingRegistrationRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.UserRepository;
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
    private final EmailService emailService;
    private final PendingRegistrationRepository pendingRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    @Transactional
    public String createUser(UserRequestDto requestDto) {
        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (pendingRepository.existsByUsername(requestDto.getUsername())) {
            throw new IllegalArgumentException(
                    "Username đang chờ xác thực"
            );
        }

        pendingRepository.findByEmail(requestDto.getEmail())
                .ifPresent(pending -> {
                    if (pending.getExpiryDate().isBefore(LocalDateTime.now())) {
                        pendingRepository.delete(pending);
                    }
                });

        if (pendingRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException(
                    "Email đang chờ xác thực"
            );
        }
        // Tạo mã OTP 6 số
        String otp = String.format("%06d",
                new java.util.Random().nextInt(1000000));

        PendingRegistration pending = PendingRegistration.builder()
                .username(requestDto.getUsername())
                .passwordHash(passwordEncoder.encode(requestDto.getPassword()))
                .fullName(requestDto.getFullName())
                .email(requestDto.getEmail())
                .phone(requestDto.getPhone())
                .gender(requestDto.getGender())
                .dateOfBirth(requestDto.getDateOfBirth())
                .avatar(requestDto.getAvatar())
                .otpCode(otp)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .lastOtpSentAt(LocalDateTime.now())
                .build();

        pendingRepository.save(pending);

        emailService.sendOtpEmail(
                requestDto.getEmail(),
                otp
        );

        return "OTP đã được gửi tới email";
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

        PendingRegistration pending = pendingRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy yêu cầu đăng ký"));

        if (pending.getExpiryDate().isBefore(LocalDateTime.now())) {

            pendingRepository.delete(pending);

            throw new IllegalArgumentException(
                    "Mã xác thực đã hết hạn");
        }

        if (!pending.getOtpCode().equals(otp)) {
            return false;
        }

        User user = User.builder()
                .username(pending.getUsername())
                .passwordHash(pending.getPasswordHash())
                .fullName(pending.getFullName())
                .email(pending.getEmail())
                .phone(pending.getPhone())
                .gender(pending.getGender())
                .dateOfBirth(pending.getDateOfBirth())
                .avatar(pending.getAvatar())
                .status(UserStatus.ACTIVE)
                .role(roleService.getRoleEntityByName(RoleName.CUSTOMER))
                .build();

        userRepository.save(user);

        pendingRepository.delete(pending);

        return true;
    }
    @Override
    @Transactional
    public UserResponseDto updateUserStatus(Long id, UserStatus status) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id));

        if (user.getStatus() == status) {
            throw new IllegalArgumentException(
                    "User is already in status " + status);
        }

        user.setStatus(status);

        return userMapper.toDto(
                userRepository.save(user)
        );
    }
    @Override
    @Transactional
    public void resendOtp(String email) {

        PendingRegistration pending =
                pendingRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy yêu cầu đăng ký"));
        if (pending.getLastOtpSentAt() != null
                && pending.getLastOtpSentAt()
                .plusSeconds(60)
                .isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Vui lòng đợi 60 giây trước khi gửi lại OTP");
        }

        String otp = String.format(
                "%06d",
                new java.util.Random()
                        .nextInt(1000000)
        );

        pending.setOtpCode(otp);

        pending.setExpiryDate(
                LocalDateTime.now().plusMinutes(5)
        );
        pending.setLastOtpSentAt(
                LocalDateTime.now()
        );
        pendingRepository.save(pending);

        emailService.sendOtpEmail(
                email,
                otp
        );
    }

    @Override
    @Transactional
    public void changePassword(
            String username,
            ChangePasswordRequest request
    ) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPasswordHash()
        )) {
            throw new IllegalArgumentException(
                    "Mật khẩu hiện tại không đúng");
        }

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            throw new IllegalArgumentException(
                    "Xác nhận mật khẩu không khớp");
        }

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);
    }
    @Override
    @Transactional
    public void forgotPassword(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Email không tồn tại"));

        passwordResetTokenRepository.findByEmail(email)
                .ifPresent(passwordResetTokenRepository::delete);

        String otp = String.format(
                "%06d",
                new java.util.Random()
                        .nextInt(1000000)
        );

        PasswordResetToken token =
                PasswordResetToken.builder()
                        .email(email)
                        .otpCode(otp)
                        .expiryDate(
                                LocalDateTime.now()
                                        .plusMinutes(5)
                        )
                        .lastOtpSentAt(
                                LocalDateTime.now()
                        )
                        .failedAttempts(0)
                        .build();

        passwordResetTokenRepository.save(token);

        emailService.sendOtpEmail(
                email,
                otp
        );
    }
    @Override
    @Transactional
    public void resendResetOtp(String email) {

        PasswordResetToken token =
                passwordResetTokenRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy yêu cầu reset mật khẩu"));

        if (token.getLastOtpSentAt() != null
                && token.getLastOtpSentAt()
                .plusSeconds(60)
                .isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Vui lòng đợi 60 giây trước khi gửi lại OTP");
        }

        String otp = String.format(
                "%06d",
                new java.util.Random()
                        .nextInt(1000000)
        );

        token.setOtpCode(otp);

        token.setExpiryDate(
                LocalDateTime.now()
                        .plusMinutes(5)
        );

        token.setLastOtpSentAt(
                LocalDateTime.now()
        );

        token.setFailedAttempts(0);

        passwordResetTokenRepository.save(token);

        emailService.sendOtpEmail(
                email,
                otp
        );
    }
    @Override
    @Transactional
    public void resetPassword(
            ResetPasswordRequest request
    ) {

        PasswordResetToken token =
                passwordResetTokenRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy OTP"));

        if (token.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            passwordResetTokenRepository
                    .delete(token);

            throw new IllegalArgumentException(
                    "OTP đã hết hạn");
        }

        if (token.getFailedAttempts() >= 5) {

            passwordResetTokenRepository
                    .delete(token);

            throw new IllegalArgumentException(
                    "OTP đã bị khóa");
        }

        if (!token.getOtpCode()
                .equals(request.getOtp())) {

            token.setFailedAttempts(
                    token.getFailedAttempts() + 1
            );

            passwordResetTokenRepository
                    .save(token);

            throw new IllegalArgumentException(
                    "OTP không chính xác");
        }

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            throw new IllegalArgumentException(
                    "Xác nhận mật khẩu không khớp");
        }

        User user =
                userRepository.findByEmail(
                        request.getEmail()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);

        passwordResetTokenRepository
                .delete(token);
    }
}
