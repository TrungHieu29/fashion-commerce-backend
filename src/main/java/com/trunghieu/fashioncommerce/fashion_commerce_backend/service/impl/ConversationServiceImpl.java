package com.trunghieu.fashioncommerce.fashion_commerce_backend.service.impl;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ConversationRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ConversationResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Conversation;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Shop;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.User;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.exception.ResourceNotFoundException;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ConversationRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.ShopRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.repository.UserRepository;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    @Override
    @Transactional
    public ConversationResponseDto createConversation(ConversationRequestDto requestDto) {
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + requestDto.getUserId()));

        Shop shop = shopRepository.findById(requestDto.getShopId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with id: " + requestDto.getShopId()));

        if (shop.getOwner() != null && shop.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Shop owner cannot create a conversation with their own shop");
        }

        // Mỗi khách hàng có thể chat với nhiều shop, và mỗi shop có thể chat với nhiều khách hàng.
        if (conversationRepository.findByUserIdAndShopId(user.getId(), shop.getId()).isPresent()) {
            throw new IllegalArgumentException("Conversation already exists between user and shop");
        }

        Conversation conversation = Conversation.builder()
                .user(user)
                .shop(shop)
                .build();

        Conversation saved = conversationRepository.save(conversation);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponseDto getConversationById(Long id) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + id));
        return toDto(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationResponseDto> getConversationsByUserId(Long userId, Pageable pageable) {
        return conversationRepository.findByUserId(userId, pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationResponseDto> getConversationsByShopId(Long shopId, Pageable pageable) {
        return conversationRepository.findByShopId(shopId, pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional
    public ConversationResponseDto getOrCreateConversation(Long userId, Long shopId) {
        return conversationRepository.findByUserIdAndShopId(userId, shopId)
                .map(this::toDto)
                .orElseGet(() -> createConversation(new ConversationRequestDto(userId, shopId)));
    }

    private ConversationResponseDto toDto(Conversation conversation) {
        return ConversationResponseDto.builder()
                .id(conversation.getId())
                .userId(conversation.getUser().getId())
                .userName(conversation.getUser().getFullName())
                .userAvatar(conversation.getUser().getAvatar())
                .shopId(conversation.getShop().getId())
                .shopName(conversation.getShop().getShopName())
                .shopLogo(conversation.getShop().getLogo())
                .createdAt(conversation.getCreatedAt())
                .build();
    }
}
