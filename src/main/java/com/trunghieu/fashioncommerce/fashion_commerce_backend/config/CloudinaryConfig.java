package com.trunghieu.fashioncommerce.fashion_commerce_backend.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "fashion-ecomerce");
        config.put("api_key", "754788965944182");
        config.put("api_secret", "0OTUNd1jEyU5E_UAcK9wVfziPPo");
        return new Cloudinary(config);
    }
}