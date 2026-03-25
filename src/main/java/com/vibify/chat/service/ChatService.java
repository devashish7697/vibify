package com.vibify.chat.service;

import com.vibify.chat.dto.ChatMessageRequest;
import com.vibify.chat.dto.ChatMessageResponse;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.List;
import java.util.UUID;

public interface ChatService {

    ChatMessageResponse sendMessage(
            UUID roomId,
            ChatMessageRequest request,
            String username
    );

    void deleteMessage(UUID roomId, Long messageId, String username);

    boolean markMessagesAsSeen(UUID roomId, String username, Long lastSeenMessageId);

    List<ChatMessageResponse> getMessages(UUID roomId, Long userId, int page, int size);

}
