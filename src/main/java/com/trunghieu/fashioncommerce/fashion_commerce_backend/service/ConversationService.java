package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ConversationRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ConversationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConversationService {
    ConversationResponseDto createConversation(ConversationRequestDto requestDto);
    ConversationResponseDto getConversationById(Long id);
    Page<ConversationResponseDto> getConversationsByUserId(Long userId, Pageable pageable);
    Page<ConversationResponseDto> getConversationsByShopId(Long shopId, Pageable pageable);
    ConversationResponseDto getOrCreateConversation(Long userId, Long shopId);
}
