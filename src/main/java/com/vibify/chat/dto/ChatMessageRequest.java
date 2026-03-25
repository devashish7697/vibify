package com.vibify.chat.dto;

import com.vibify.chat.model.ChatMessageType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {

    private String content; // text or caption

    private ChatMessageType messageType;

    private String mediaUrl; // null for TEXT
}