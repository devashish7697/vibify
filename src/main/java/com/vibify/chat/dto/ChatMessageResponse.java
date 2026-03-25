package com.vibify.chat.dto;

import com.vibify.chat.model.ChatMessageType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

    private Long id;
    private UUID roomId;

    private Long senderId;
    private String senderUsername;           // ✅ ADD
    private String senderProfileImage;

    private String content;
    private ChatMessageType messageType;
    private String mediaUrl;

    private Boolean isDeleted;
    private Long createdAt;

    // 🔥 Current user's status (important)
    private String status;
}