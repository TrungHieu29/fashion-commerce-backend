package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ShippingAddressRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ShippingAddressResponseDto;
import java.util.List;

public interface ShippingAddressService {
    ShippingAddressResponseDto createShippingAddress(ShippingAddressRequestDto requestDto);
    ShippingAddressResponseDto getShippingAddressById(Long id);
    List<ShippingAddressResponseDto> getShippingAddressesByUserId(Long userId); // Lấy tất cả địa chỉ của một user
    List<ShippingAddressResponseDto> getAllShippingAddresses();
    ShippingAddressResponseDto updateShippingAddress(Long id, ShippingAddressRequestDto requestDto);
    void deleteShippingAddress(Long id);
    ShippingAddressResponseDto setDefaultShippingAddress(Long id, Long userId); // Đặt địa chỉ mặc định
}
