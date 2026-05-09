package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.MessageReadRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.MessageRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.TypingIndicatorDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.MessageReadResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.MessageResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.security.CustomUserDetails;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(MessageRequestDto messageRequestDto, Principal principal) {
        Long senderId = extractUserId(principal);
        messageRequestDto.setSenderId(senderId);

        MessageResponseDto savedMessage = messageService.sendMessage(messageRequestDto);
        String destination = "/topic/conversations/" + savedMessage.getConversationId();
        messagingTemplate.convertAndSend(destination, savedMessage);
    }

    @MessageMapping("/chat.typing")
    public void typing(TypingIndicatorDto typingIndicatorDto, Principal principal) {
        typingIndicatorDto.setSenderId(extractUserId(principal));
        String destination = "/topic/conversations/" + typingIndicatorDto.getConversationId() + "/typing";
        messagingTemplate.convertAndSend(destination, typingIndicatorDto);
    }

    @MessageMapping("/chat.markRead")
    public void markRead(MessageReadRequestDto readRequestDto, Principal principal) {
        Long readerId = extractUserId(principal);
        MessageReadResponseDto responseDto = messageService.markConversationMessagesAsRead(
                readRequestDto.getConversationId(), readerId);

        String destination = "/topic/conversations/" + responseDto.getConversationId() + "/read";
        messagingTemplate.convertAndSend(destination, responseDto);
    }

    private Long extractUserId(Principal principal) {
        if (principal instanceof Authentication authentication) {
            Object principalObj = authentication.getPrincipal();
            if (principalObj instanceof CustomUserDetails customUserDetails) {
                return customUserDetails.getId();
            }
        }
        throw new IllegalArgumentException("Unauthenticated WebSocket user");
    }
}
