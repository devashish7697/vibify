package com.vibify.websocket.security;

import com.vibify.room.repository.RoomMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final  RoomMemberRepository roomMemberRepository;

    /**
     * Intercepts every inbound STOMP message.
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (command == null) {
            return message;
        }

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (command == StompCommand.CONNECT) {
            log.info("CONNECT received - allowing handshake");
            return message;
        }

        // AFTER CONNECT → validate
        if (sessionAttributes == null) {
            log.warn("WebSocket message rejected: Missing session attributes");
            throw new IllegalArgumentException("Unauthorized WebSocket session");
        }

        Object userIdObj = sessionAttributes.get("userId");
        Object usernameObj = sessionAttributes.get("username");

        if (userIdObj == null || usernameObj == null) {
            log.warn("WebSocket message rejected: Missing authentication attributes");
            throw new IllegalArgumentException("Unauthorized WebSocket access");
        }

        Long userId = (Long) userIdObj;
        String username = (String) usernameObj;

        /*
         * Attach Principal to message context.
         * Controllers can retrieve authenticated user via Principal.
         */
        accessor.setLeaveMutable(true);
        accessor.setUser(new StompPrincipal(userId, username));

        switch (command) {

            case CONNECT -> log.debug("WebSocket CONNECT authenticated for userId={}", userId);

            case SUBSCRIBE -> {

                String destination = accessor.getDestination();

                log.debug("WebSocket SUBSCRIBE userId={} destination={}",
                        userId, destination);

                if (destination == null || !destination.startsWith("/topic/")) {
                    log.warn("Invalid subscription destination: {}", destination);
                    throw new IllegalArgumentException("Invalid subscription destination");
                }

                UUID roomId = extractRoomId(destination);

                boolean isMember =
                        roomMemberRepository.existsByRoomIdAndUserIdAndLeftAtIsNull(
                                roomId, userId);

                if (!isMember) {
                    log.warn("User {} tried to subscribe without membership", userId);
                    throw new IllegalArgumentException("Not authorized to subscribe");
                }
            }

            case SEND -> {

                String destination = accessor.getDestination();

                log.debug("WebSocket SEND userId={} destination={}",
                        userId, destination);

                if (destination == null || !destination.startsWith("/app/room/")) {
                    log.warn("Invalid message destination: {}", destination);
                    throw new IllegalArgumentException("Invalid message destination");
                }
            }

            default -> {
                // other commands (DISCONNECT etc) are allowed
            }
        }

        return message;
    }

    /**
     * Custom Principal implementation for WebSocket sessions.
     */
    public static class StompPrincipal implements Principal {

        private final Long userId;
        private final String username;

        public StompPrincipal(Long userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        public Long getUserId() {
            return userId;
        }

        @Override
        public String getName() {
            return username;
        }
    }

    private UUID extractRoomId(String destination) {

        try {
            String[] parts = destination.split("/");

            if (parts.length < 4) {
                throw new IllegalArgumentException("Invalid destination format");
            }

            return UUID.fromString(parts[3]);

        } catch (Exception e) {
            log.warn("Failed to extract roomId from destination={}", destination);
            throw new IllegalArgumentException("Invalid room subscription");
        }
    }
}