package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async // Vẫn giữ tính năng chạy ngầm
    public void sendOtpEmail(String to, String otp) {
        String url = "https://api.brevo.com/v3/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        // Chuẩn bị nội dung
        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("email", "babie29102005@gmail.com", "name", "FashionCommerce"));
        body.put("to", new Object[]{Map.of("email", to)});
        body.put("subject", "Mã xác nhận tài khoản FashionCommerce");
        body.put("htmlContent", "<h3>Chào bạn,</h3><p>Mã OTP của bạn là: <b>" + otp + "</b><br>Thời hạn: 5 phút.</p>");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, entity, String.class);
        } catch (Exception e) {
            // Log lỗi nếu gửi thất bại
            e.printStackTrace();
        }
    }
}