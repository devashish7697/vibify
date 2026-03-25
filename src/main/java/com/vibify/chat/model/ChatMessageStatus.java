package com.vibify.chat.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_message_status",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_message_user", columnNames = {"message_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_message", columnList = "message_id"),
                @Index(name = "idx_user", columnList = "user_id")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 Message reference
    @Column(name = "message_id", nullable = false)
    private Long messageId;

    // 🔹 User reference
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 🔹 Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ChatMessageStatusType status;

    // 🔹 Timestamp of last update
    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;
}