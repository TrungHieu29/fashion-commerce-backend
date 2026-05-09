package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ReviewRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ReviewResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewResponseDto createReview(ReviewRequestDto requestDto);
    ReviewResponseDto getReviewById(Long id);
    Page<ReviewResponseDto> getReviewsByProductId(Long productId, Pageable pageable);
    Page<ReviewResponseDto> getReviewsByUserId(Long userId, Pageable pageable);
    ReviewResponseDto updateReview(Long id, ReviewRequestDto requestDto);
    void deleteReview(Long id);
}
