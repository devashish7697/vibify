package com.vibify.room.service;

import com.vibify.room.dto.RoomQueueItemDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface RoomQueueService {

    /**
     * Add a song to the room queue.
     */
    RoomQueueItemDto addSongToQueue(UUID roomId, Long songId, Long userId);

    /**
     * Remove a song from the queue.
     */
    void removeSongFromQueue(UUID roomId, Long queueItemId, Long userId);

    /**
     * Reorder queue item (used for drag-drop in UI).
     */
    void reorderQueueItem(UUID roomId, Long queueItemId, Integer newOrderIndex, Long userId);

    /**
     * Fetch full queue for a room.
     */
    List<RoomQueueItemDto> getRoomQueue(UUID roomId);

    /**
     * Fetch the next song in queue based on current order.
     */
    RoomQueueItemDto getNextQueueItem(UUID roomId, Integer currentOrderIndex);

    @Transactional
    void clearQueue(UUID roomId);
}
