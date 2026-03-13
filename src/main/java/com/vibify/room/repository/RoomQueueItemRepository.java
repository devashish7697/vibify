package com.vibify.room.repository;

import com.vibify.room.model.room_queue.RoomQueueItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomQueueItemRepository
        extends JpaRepository<RoomQueueItem, Long> {

    // Fetch full queue
    List<RoomQueueItem> findByRoomIdOrderByOrderIndexAsc(UUID roomId);

    // Get next queue item (for skip / auto next)
    Optional<RoomQueueItem> findFirstByRoomIdAndOrderIndexGreaterThanOrderByOrderIndexAsc(
            UUID roomId, Integer orderIndex
    );

    // Get last item (used when adding new song)
    Optional<RoomQueueItem> findFirstByRoomIdOrderByOrderIndexDesc(UUID roomId);

    // Delete queue when new playlist/song starts
    void deleteByRoomId(UUID roomId);
}
