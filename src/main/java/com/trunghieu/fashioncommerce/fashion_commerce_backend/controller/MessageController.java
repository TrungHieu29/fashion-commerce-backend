package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.MessageRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.MessageResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or #requestDto.senderId == authentication.principal.id")
    public ResponseEntity<MessageResponseDto> sendMessage(@Valid @RequestBody MessageRequestDto requestDto) {
        return new ResponseEntity<>(messageService.sendMessage(requestDto), HttpStatus.CREATED);
    }

    @GetMapping("/conversations/{conversationId}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isConversationParticipant(#conversationId)")
    public ResponseEntity<Page<MessageResponseDto>> getMessagesByConversation(
            @PathVariable Long conversationId,
            Pageable pageable) {
        return ResponseEntity.ok(messageService.getMessagesByConversationId(conversationId, pageable));
    }

    @PutMapping("/conversations/{conversationId}/read")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isConversationParticipant(#conversationId)")
    public ResponseEntity<Void> markConversationRead(@PathVariable Long conversationId) {
        messageService.markConversationMessagesAsRead(conversationId);
        return ResponseEntity.noContent().build();
    }
}
