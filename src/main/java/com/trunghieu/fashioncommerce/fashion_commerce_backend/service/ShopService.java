package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ShopRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ShopStatusRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ShopResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.enums.ShopStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ShopService {
    ShopResponseDto createShop(ShopRequestDto requestDto, MultipartFile logo) throws IOException;
    ShopResponseDto getShopById(Long id);
    ShopResponseDto getShopByOwnerId(Long ownerId); // Thêm phương thức này để lấy shop của một user
    List<ShopResponseDto> getAllShops();
    ShopResponseDto updateShop(Long id, ShopRequestDto requestDto);
    void deleteShop(Long id);
    ShopResponseDto updateShopStatus(Long id, ShopStatus status);
}
