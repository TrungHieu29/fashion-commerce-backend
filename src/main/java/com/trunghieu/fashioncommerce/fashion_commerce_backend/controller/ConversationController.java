package com.trunghieu.fashioncommerce.fashion_commerce_backend.controller;

import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request.ConversationRequestDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response.ConversationResponseDto;
import com.trunghieu.fashioncommerce.fashion_commerce_backend.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or #requestDto.userId == authentication.principal.id")
    public ResponseEntity<ConversationResponseDto> createConversation(
            @Valid @RequestBody ConversationRequestDto requestDto) {
        return new ResponseEntity<>(conversationService.createConversation(requestDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isConversationParticipant(#id)")
    public ResponseEntity<ConversationResponseDto> getConversationById(@PathVariable Long id) {
        return ResponseEntity.ok(conversationService.getConversationById(id));
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<Page<ConversationResponseDto>> getConversationsByUserId(
            @PathVariable Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(conversationService.getConversationsByUserId(userId, pageable));
    }

    @GetMapping("/shops/{shopId}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isShopOwner(#shopId)")
    public ResponseEntity<Page<ConversationResponseDto>> getConversationsByShopId(
            @PathVariable Long shopId,
            Pageable pageable) {
        return ResponseEntity.ok(conversationService.getConversationsByShopId(shopId, pageable));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id or @securityUtils.isShopOwner(#shopId)")
    public ResponseEntity<ConversationResponseDto> getOrCreateConversation(
            @RequestParam Long userId,
            @RequestParam Long shopId) {
        return ResponseEntity.ok(conversationService.getOrCreateConversation(userId, shopId));
    }
}
