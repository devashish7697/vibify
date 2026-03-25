package com.vibify.chat.controller;

import com.vibify.auth.security.UserPrincipal;
import com.vibify.chat.dto.*;
import com.vibify.chat.service.ChatService;
import com.vibify.common.exception.UserNotFoundException;
import com.vibify.common.globalResponse.GlobalApiResponse;
import com.vibify.user.model.User;
import com.vibify.user.repository.UserRepository;
import com.vibify.websocket.dto.WsEvent;
import com.vibify.websocket.event.RoomEventPublisher;
import com.vibify.websocket.event.RoomEventType;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;
    private final RoomEventPublisher eventPublisher;
    private final UserRepository userRepository;

    public ChatWebSocketController(
            ChatService chatService,
            RoomEventPublisher eventPublisher,
            UserRepository userRepository
    ) {
        this.chatService = chatService;
        this.eventPublisher = eventPublisher;
        this.userRepository = userRepository;
    }

    // ---------------- SEND MESSAGE ----------------

    @MessageMapping("/room/{roomId}/chat/send")
    public void sendMessage(
            @DestinationVariable UUID roomId,
            @Payload ChatMessageRequest request,
            Principal principal
    ) {

        String username = principal.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Long userId = user.getId();

        ChatMessageResponse response =
                chatService.sendMessage(roomId, request, username);

        WsEvent<ChatMessageResponse> event = WsEvent.of(
                RoomEventType.CHAT_MESSAGE_SENT,
                roomId,
                userId,
                null,
                response
        );

        eventPublisher.publishChatEvent(roomId, event);
    }

    // ---------------- SEEN ----------------

    @MessageMapping("/room/{roomId}/chat/seen")
    public void markSeen(
            @DestinationVariable UUID roomId,
            @Payload ChatSeenRequest request,
            Principal principal
    ) {

        String username = principal.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Long userId = user.getId();

        boolean updated = chatService.markMessagesAsSeen(
                roomId,
                username,
                request.getLastSeenMessageId()
        );

        if (!updated) {
            return; // ❌ no event
        }

        WsEvent<ChatSeenRequest> event = WsEvent.of(
                RoomEventType.CHAT_MESSAGE_SEEN,
                roomId,
                userId,
                null,
                request
        );

        eventPublisher.publishChatEvent(roomId, event);
    }

    // ---------------- DELETE MESSAGE ----------------

    @MessageMapping("/room/{roomId}/chat/delete")
    public void deleteMessage(
            @DestinationVariable UUID roomId,
            @Payload ChatDeleteRequest request,
            Principal principal
    ) {

        String username = principal.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Long userId = user.getId();

        chatService.deleteMessage(
                roomId,
                request.getMessageId(),
                username
        );

        WsEvent<ChatDeleteRequest> event = WsEvent.of(
                RoomEventType.CHAT_MESSAGE_DELETED,
                roomId,
                userId,
                null,
                request
        );

        eventPublisher.publishChatEvent(roomId, event);
    }

    // ---------------- TYPING ----------------

    @MessageMapping("/room/{roomId}/chat/typing")
    public void typing(
            @DestinationVariable UUID roomId,
            @Payload TypingEventRequest request,
            Principal principal
    ) {

        String username = principal.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Long userId = user.getId();

        RoomEventType type = Boolean.TRUE.equals(request.getTyping())
                ? RoomEventType.TYPING_STARTED
                : RoomEventType.TYPING_STOPPED;

        WsEvent<Void> event = WsEvent.of(
                type,
                roomId,
                userId,
                null,
                null
        );

        eventPublisher.publishChatEvent(roomId, event);
    }


    /// get all Chat message history
    @GetMapping("/room/{roomId}/messages")
    public GlobalApiResponse<List<ChatMessageResponse>> getMessages(
            @PathVariable UUID roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        Long userId = user.getId();

        List<ChatMessageResponse> messages =
                chatService.getMessages(roomId, userId, page, size);

        return GlobalApiResponse.success("Messages Fetched Successfully", messages);
    }
}