package com.trunghieu.fashioncommerce.fashion_commerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponseDto {
    private Long id;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Long shopId;
    private String shopName;
    private String shopLogo;
    private LocalDateTime createdAt;
    // Có thể thêm LastMessageResponseDto để hiển thị tin nhắn cuối cùng trong danh sách
}