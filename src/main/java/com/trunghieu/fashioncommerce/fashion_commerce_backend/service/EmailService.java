package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Async // Không làm treo luồng chính
    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Mã xác nhận tài khoản FashionCommerce");
        message.setText("Chào bạn, mã OTP của bạn là: " + otp + "\nThời hạn: 5 phút.");
        mailSender.send(message);
    }
}