package com.trunghieu.fashioncommerce.fashion_commerce_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync        // Kích hoạt tính năng chạy bất đồng bộ (cho gửi mail)
@EnableScheduling   // Kích hoạt tính năng lên lịch (cho dọn dẹp token cũ)
@SpringBootApplication
public class FashionCommerceBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FashionCommerceBackendApplication.class, args);
	}

}
