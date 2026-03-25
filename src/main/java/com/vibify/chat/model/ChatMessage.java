package com.vibify.chat.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "chat_messages",
        indexes = {
                @Index(name = "idx_room_created", columnList = "room_id, created_at DESC"),
                @Index(name = "idx_sender", columnList = "sender_id")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 Room reference
    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    // 🔹 Sender
    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    // 🔹 Message content (text or caption)
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // 🔹 Message type
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private ChatMessageType messageType;

    // 🔹 Media URL (image/video/audio)
    @Column(name = "media_url")
    private String mediaUrl;

    // 🔹 Soft delete flag
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Builder.Default
    @Column(name = "media_deleted", nullable = false)
    private boolean mediaDeleted = false;

    @Column(name = "deleted_at")
    private Long deletedAt;

    // 🔹 Timestamp
    @Column(name = "created_at", nullable = false)
    private Long createdAt;
}