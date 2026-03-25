package com.vibify.chat.repository;

import com.vibify.chat.model.ChatMessageStatus;
import com.vibify.chat.model.ChatMessageStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChatMessageStatusRepository extends JpaRepository<ChatMessageStatus, Long> {

    // 🔥 Fetch statuses for messages (used in mapping)
    List<ChatMessageStatus> findByMessageIdInAndUserId(List<Long> messageIds, Long userId);


    // 🔥 Batch update to SEEN (CRITICAL)
    @Modifying
    @Transactional
    @Query("""
    UPDATE ChatMessageStatus s
    SET s.status = :status, s.updatedAt = :timestamp
    WHERE s.messageId IN :messageIds
      AND s.userId = :userId
      AND s.status <> :status
    """)
    int updateStatusByMessageIdsAndUserId(
            List<Long> messageIds,
            Long userId,
            ChatMessageStatusType status,
            Long timestamp
    );
}