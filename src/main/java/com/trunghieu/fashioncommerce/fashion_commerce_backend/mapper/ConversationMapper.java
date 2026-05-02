package com.trunghieu.fashioncommerce.fashion_commerce_backend.mapper;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.entity.Conversation;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ConversationRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ConversationResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "messages", ignore = true)
    Conversation toEntity(ConversationRequestDto dto);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userName")
    @Mapping(source = "user.avatar", target = "userAvatar")
    @Mapping(source = "shop.id", target = "shopId")
    @Mapping(source = "shop.shopName", target = "shopName")
    @Mapping(source = "shop.logo", target = "shopLogo")
    ConversationResponseDto toDto(Conversation entity);
}
