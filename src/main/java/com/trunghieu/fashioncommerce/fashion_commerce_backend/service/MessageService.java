package com.trunghieu.fashioncommerce.fashion_commerce_backend.service;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.MessageReadRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.MessageRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.MessageReadResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.MessageResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {
    MessageResponseDto sendMessage(MessageRequestDto requestDto);
    Page<MessageResponseDto> getMessagesByConversationId(Long conversationId, Pageable pageable);
    void markConversationMessagesAsRead(Long conversationId);
    MessageReadResponseDto markConversationMessagesAsRead(Long conversationId, Long readerId);
}
