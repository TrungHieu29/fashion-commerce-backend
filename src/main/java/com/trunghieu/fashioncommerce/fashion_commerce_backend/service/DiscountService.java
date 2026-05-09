package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.DiscountRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.DiscountResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiscountService {
    DiscountResponseDto createDiscount(DiscountRequestDto requestDto);
    DiscountResponseDto getDiscountById(Long id);
    Page<DiscountResponseDto> getDiscountsByShopId(Long shopId, Pageable pageable);
    List<DiscountResponseDto> getActiveDiscountsByShopId(Long shopId);
    DiscountResponseDto updateDiscount(Long id, DiscountRequestDto requestDto);
    void deleteDiscount(Long id);
}
