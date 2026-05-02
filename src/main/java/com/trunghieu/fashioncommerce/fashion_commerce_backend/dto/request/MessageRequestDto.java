package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequestDto {
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;
    @NotNull(message = "Sender ID is required")
    private Long senderId; // ID của người gửi (User hoặc Shop)
    @NotBlank(message = "Message content cannot be blank")
    private String content;
}