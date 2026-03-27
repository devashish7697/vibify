package com.vibify.chat.service;

import com.vibify.chat.dto.ChatMessageRequest;
import com.vibify.chat.dto.ChatMessageResponse;
import com.vibify.chat.model.*;
import com.vibify.chat.repository.ChatMessageRepository;
import com.vibify.chat.repository.ChatMessageStatusRepository;
import com.vibify.common.exception.UserNotFoundException;
import com.vibify.room.model.room_member.RoomMember;
import com.vibify.room.repository.RoomMemberRepository;
import com.vibify.room.service.RoomService;
import com.vibify.user.model.User;
import com.vibify.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageStatusRepository chatMessageStatusRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final UserRepository userRepository;
    private final RoomService roomService;

    public ChatServiceImpl(
            ChatMessageRepository chatMessageRepository,
            ChatMessageStatusRepository chatMessageStatusRepository,
            RoomMemberRepository roomMemberRepository,
                    UserRepository userRepository,
            RoomService roomService
    ) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatMessageStatusRepository = chatMessageStatusRepository;
        this.roomMemberRepository = roomMemberRepository;
        this.userRepository = userRepository;
        this.roomService = roomService;
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(UUID roomId, ChatMessageRequest request, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Long senderId = user.getId();

        // 🔴 1. Membership validation
        boolean isMember = roomMemberRepository
                .existsByRoomIdAndUserIdAndLeftAtIsNull(roomId, senderId);

        if (!isMember) {
            throw new RuntimeException("User is not a member of this room");
        }

        // 🔴 2. Validate request
        validateMessageRequest(request);

        long now = System.currentTimeMillis();

        // 🔹 3. Persist message
        ChatMessage message = ChatMessage.builder()
                .roomId(roomId)
                .senderId(senderId)
                .content(request.getContent())
                .messageType(request.getMessageType())
                .mediaUrl(request.getMediaUrl())
                .isDeleted(false)
                .createdAt(now)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        // 🔹 4. Fetch active members
        List<Long> userIds = roomMemberRepository
                .findByRoomIdAndLeftAtIsNull(roomId)
                .stream()
                .map(RoomMember::getUserId)
                .collect(Collectors.toList());

        // 🔹 5. Create status entries (batch)
        List<ChatMessageStatus> statuses = userIds.stream()
                .map(userId -> ChatMessageStatus.builder()
                        .messageId(savedMessage.getId())
                        .userId(userId)
                        .status(userId.equals(senderId)
                                ? ChatMessageStatusType.SENT
                                : ChatMessageStatusType.DELIVERED)
                        .updatedAt(now)
                        .build()
                ).collect(Collectors.toList());

        chatMessageStatusRepository.saveAll(statuses);
        roomService.updateRoomActivity(roomId);

        // 🔹 6. Return response
        return ChatMessageResponse.builder()
                .id(savedMessage.getId())
                .roomId(roomId)
                .senderId(senderId)
                .senderUsername(sender.getUsername())
                .senderProfileImage(sender.getProfileImage())
                .content(savedMessage.getContent())
                .messageType(savedMessage.getMessageType())
                .mediaUrl(savedMessage.getMediaUrl())
                .isDeleted(savedMessage.getIsDeleted())
                .createdAt(savedMessage.getCreatedAt())
                .status(ChatMessageStatusType.SENT.name()) // sender perspective
                .build();
    }

    @Override
    @Transactional
    public void deleteMessage(UUID roomId, Long messageId, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Long userId = user.getId();

        // 🔴 1. Fetch message
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // 🔴 2. Validate room match (security)
        if (!message.getRoomId().equals(roomId)) {
            throw new RuntimeException("Invalid room for this message");
        }

        // 🔴 3. Membership validation
        boolean isMember = roomMemberRepository
                .existsByRoomIdAndUserIdAndLeftAtIsNull(roomId, userId);

        if (!isMember) {
            throw new RuntimeException("User is not a member of this room");
        }

        // 🔴 4. Permission check (ONLY sender)
        if (!message.getSenderId().equals(userId)) {
            throw new RuntimeException("User not allowed to delete this message");
        }

        // 🔴 5. Already deleted check (important)
        if (Boolean.TRUE.equals(message.getIsDeleted())) {
            return; // idempotent
        }

        long now = System.currentTimeMillis();

        // 🔹 6. Soft delete
        message.setIsDeleted(true);
        message.setDeletedAt(now);

        chatMessageRepository.save(message);
        roomService.updateRoomActivity(roomId);
    }

    @Override
    @Transactional
    public boolean markMessagesAsSeen(UUID roomId, String username, Long lastSeenMessageId) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Long userId = user.getId();

        // 🔴 1. Membership validation
        boolean isMember = roomMemberRepository
                .existsByRoomIdAndUserIdAndLeftAtIsNull(roomId, userId);

        if (!isMember) {
            throw new RuntimeException("User is not a member of this room");
        }

        if (lastSeenMessageId == null) {
            return false;
        }

        long now = System.currentTimeMillis();

        // 🔹 2. Fetch message IDs
        List<Long> messageIds = chatMessageRepository
                .findIdsByRoomIdAndIdLessThanEqual(roomId, lastSeenMessageId);

        if (messageIds.isEmpty()) {
            return false;
        }

        // 🔹 3. Batch update
        int updatedRows = chatMessageStatusRepository
                .updateStatusByMessageIdsAndUserId(
                        messageIds,
                        userId,
                        ChatMessageStatusType.SEEN,
                        now
                );

        roomService.updateRoomActivity(roomId);

        // 🔥 4. RETURN WHETHER UPDATED
        return updatedRows > 0;
    }


    /// get history api,
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(
            UUID roomId,
            Long userId,
            int page,
            int size
    ) {

        // 🔴 1. Membership validation (fail fast)
        if (!roomMemberRepository.existsByRoomIdAndUserIdAndLeftAtIsNull(roomId, userId)) {
            throw new RuntimeException("User is not a member of this room");
        }

        // 🔹 2. Pagination (defensive + deterministic ordering)
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt", "id")
        );

        // 🔹 3. Fetch messages
        List<ChatMessage> messages =
                chatMessageRepository.findByRoomIdOrderByCreatedAtDescIdDesc(roomId, pageable);

        if (messages.isEmpty()) {
            return Collections.emptyList();
        }

        // 🔹 4. Collect messageIds + senderIds (single loop)
        List<Long> messageIds = new ArrayList<>(messages.size());
        Set<Long> senderIds = new HashSet<>(messages.size());

        for (ChatMessage msg : messages) {
            messageIds.add(msg.getId());

            if (msg.getSenderId() != null) {
                senderIds.add(msg.getSenderId());
            }
        }

        // 🔹 5. Fetch statuses (single query)
        List<ChatMessageStatus> statuses =
                chatMessageStatusRepository.findByMessageIdInAndUserId(messageIds, userId);

        Map<Long, ChatMessageStatusType> statusMap = new HashMap<>(statuses.size());
        for (ChatMessageStatus status : statuses) {
            statusMap.put(status.getMessageId(), status.getStatus());
        }

        // 🔹 6. Fetch users in bulk
        Map<Long, User> userMap = new HashMap<>(senderIds.size());

        if (!senderIds.isEmpty()) {
            List<User> users = userRepository.findAllById(senderIds);

            for (User user : users) {
                userMap.put(user.getId(), user);
            }
        }

        // 🔹 7. Build response
        List<ChatMessageResponse> response = new ArrayList<>(messages.size());

        for (ChatMessage msg : messages) {

            // ✅ Correct default status (important fix)
            ChatMessageStatusType status =
                    statusMap.getOrDefault(
                            msg.getId(),
                            msg.getSenderId().equals(userId)
                                    ? ChatMessageStatusType.SENT
                                    : ChatMessageStatusType.DELIVERED
                    );

            User sender = userMap.get(msg.getSenderId());

            response.add(ChatMessageResponse.builder()
                    .id(msg.getId())
                    .roomId(msg.getRoomId())

                    .senderId(msg.getSenderId())
                    .senderUsername(sender != null ? sender.getUsername() : "Unknown")
                    .senderProfileImage(sender != null ? sender.getProfileImage() : null)

                    // ✅ Safe handling for deleted messages
                    .content(Boolean.TRUE.equals(msg.getIsDeleted()) ? null : msg.getContent())
                    .mediaUrl(Boolean.TRUE.equals(msg.getIsDeleted()) ? null : msg.getMediaUrl())

                    .messageType(msg.getMessageType())
                    .isDeleted(msg.getIsDeleted())
                    .createdAt(msg.getCreatedAt())

                    .status(status.name())
                    .build());
        }

        roomService.updateRoomActivity(roomId);

        return response;
    }


    // ---------------- PRIVATE METHODS ----------------

    private void validateMessageRequest(ChatMessageRequest request) {

        if (request.getMessageType() == null) {
            throw new RuntimeException("Message type is required");
        }

        switch (request.getMessageType()) {

            case TEXT -> {
                if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                    throw new RuntimeException("Text message content cannot be empty");
                }
            }

            case IMAGE, VIDEO, AUDIO -> {
                if (request.getMediaUrl() == null || request.getMediaUrl().trim().isEmpty()) {
                    throw new RuntimeException("Media URL is required for media messages");
                }
            }

            case SYSTEM -> {
                throw new RuntimeException("SYSTEM messages cannot be sent by users");
            }
        }
    }
}