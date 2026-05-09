package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.MessageRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.MessageReadResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.MessageResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Conversation;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Message;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.User;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ConversationRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.MessageRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.UserRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MessageResponseDto sendMessage(MessageRequestDto requestDto) {
        Conversation conversation = conversationRepository.findById(requestDto.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + requestDto.getConversationId()));

        User sender = userRepository.findById(requestDto.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + requestDto.getSenderId()));

        if (!isValidSender(conversation, sender)) {
            throw new IllegalArgumentException("Sender must be either the conversation customer or the shop owner");
        }

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(requestDto.getContent())
                .isRead(false)
                .build();

        Message saved = messageRepository.save(message);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponseDto> getMessagesByConversationId(Long conversationId, Pageable pageable) {
        return messageRepository.findByConversationId(conversationId, pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional
    public void markConversationMessagesAsRead(Long conversationId) {
        markConversationMessagesAsRead(conversationId, null);
    }

    @Override
    @Transactional
    public MessageReadResponseDto markConversationMessagesAsRead(Long conversationId, Long readerId) {
        int readCount = (int) messageRepository.findByConversationIdAndIsReadFalse(conversationId)
                .stream()
                .filter(message -> readerId == null || !message.getSender().getId().equals(readerId))
                .peek(message -> message.setIsRead(true))
                .count();

        return MessageReadResponseDto.builder()
                .conversationId(conversationId)
                .readerId(readerId)
                .messagesRead(readCount)
                .build();
    }

    private boolean isValidSender(Conversation conversation, User sender) {
        Long conversationUserId = conversation.getUser().getId();
        Long shopOwnerId = conversation.getShop().getOwner() != null ? conversation.getShop().getOwner().getId() : null;
        return sender.getId().equals(conversationUserId) || (shopOwnerId != null && sender.getId().equals(shopOwnerId));
    }

    private MessageResponseDto toDto(Message message) {
        return MessageResponseDto.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .content(message.getContent())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
