package com.vibify.chat.repository;

import com.vibify.chat.model.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 🔥 Pagination (latest messages first)
    List<ChatMessage> findByRoomIdOrderByCreatedAtDescIdDesc(UUID roomId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("""
    SELECT m.id FROM ChatMessage m
    WHERE m.roomId = :roomId
    AND m.id <= :lastSeenMessageId
    AND m.isDeleted = false
    """)
    List<Long> findIdsByRoomIdAndIdLessThanEqual(
            UUID roomId,
            Long lastSeenMessageId
    );

    @org.springframework.data.jpa.repository.Query("""
    SELECT m FROM ChatMessage m
    WHERE m.isDeleted = true
      AND m.mediaDeleted = false
      AND m.mediaUrl IS NOT NULL
    """)
    List<ChatMessage> findMessagesPendingMediaCleanup();

    @org.springframework.data.jpa.repository.Query("""
    SELECT m.mediaUrl FROM ChatMessage m
    WHERE m.roomId = :roomId
      AND m.mediaUrl IS NOT NULL
""")
    List<String> findMediaUrlsByRoomId(UUID roomId);

}
