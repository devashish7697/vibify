package com.vibify.room.service;

import com.vibify.common.exception.SongNotFoundException;
import com.vibify.common.exception.room_exception.QueueItemNotFoundException;
import com.vibify.room.dto.RoomQueueItemDto;
import com.vibify.room.model.room_queue.RoomQueueItem;
import com.vibify.room.model.room_queue.QueueSourceType;
import com.vibify.room.repository.RoomQueueItemRepository;

import com.vibify.songs.model.Song;
import com.vibify.songs.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomQueueServiceImpl implements RoomQueueService {

    private static final int ORDER_GAP = 1000;

    private final RoomQueueItemRepository roomQueueItemRepository;
    private final RoomMembershipService roomMembershipService;
    private final SongRepository songRepository;

    /**
     * Add a single song to the room queue.
     */
    @Override
    @Transactional
    public RoomQueueItemDto addSongToQueue(UUID roomId, Long songId, Long userId) {

        roomMembershipService.validateUserMembership(roomId, userId);

        Integer newOrderIndex = generateNextOrderIndex(roomId);

        RoomQueueItem item = RoomQueueItem.builder()
                .roomId(roomId)
                .songId(songId)
                .addedBy(userId)
                .orderIndex(newOrderIndex)
                .sourceType(QueueSourceType.MANUAL)
                .sourceId(null)
                .createdAt(System.currentTimeMillis())
                .build();

        RoomQueueItem saved = roomQueueItemRepository.save(item);

        return mapToDto(saved);
    }

    /**
     * Remove item from queue.
     */
    @Override
    @Transactional
    public void removeSongFromQueue(UUID roomId, Long queueItemId, Long userId) {

        roomMembershipService.validateUserMembership(roomId, userId);

        RoomQueueItem item = roomQueueItemRepository.findById(queueItemId)
                .orElseThrow(() ->
                        new QueueItemNotFoundException("Queue item not found"));

        if (!item.getRoomId().equals(roomId)) {
            throw new QueueItemNotFoundException("Queue item does not belong to this room");
        }

        roomQueueItemRepository.delete(item);
    }

    /**
     * Reorder queue item using sparse ordering.
     */
    @Override
    @Transactional
    public void reorderQueueItem(UUID roomId,
                                 Long queueItemId,
                                 Integer newOrderIndex,
                                 Long userId) {

        roomMembershipService.validateUserMembership(roomId, userId);

        RoomQueueItem item = roomQueueItemRepository.findById(queueItemId)
                .orElseThrow(() ->
                        new QueueItemNotFoundException("Queue item not found"));

        if (!item.getRoomId().equals(roomId)) {
            throw new QueueItemNotFoundException("Queue item does not belong to this room");
        }

        item.setOrderIndex(newOrderIndex);

        roomQueueItemRepository.save(item);
    }

    /**
     * Fetch full room queue.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RoomQueueItemDto> getRoomQueue(UUID roomId) {

        return roomQueueItemRepository
                .findByRoomIdOrderByOrderIndexAsc(roomId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Fetch next queue item after current order.
     */
    @Override
    @Transactional(readOnly = true)
    public RoomQueueItemDto getNextQueueItem(UUID roomId, Integer currentOrderIndex) {

        RoomQueueItem next = roomQueueItemRepository
                .findFirstByRoomIdAndOrderIndexGreaterThanOrderByOrderIndexAsc(
                        roomId,
                        currentOrderIndex
                )
                .orElse(null);

        if (next == null) {
            return null;
        }

        return mapToDto(next);
    }

    /**
     * Clear room queue.
     */
    @Override
    @Transactional
    public void clearQueue(UUID roomId) {
        roomQueueItemRepository.deleteByRoomId(roomId);
    }

    /**
     * Generate next sparse order index.
     */
    private Integer generateNextOrderIndex(UUID roomId) {

        return roomQueueItemRepository
                .findFirstByRoomIdOrderByOrderIndexDesc(roomId)
                .map(item -> item.getOrderIndex() + ORDER_GAP)
                .orElse(ORDER_GAP);
    }

    /**
     * Map entity to DTO.
     */
    private RoomQueueItemDto mapToDto(RoomQueueItem item) {

        Song song = songRepository.findById(item.getSongId())
                .orElseThrow(() ->
                        new SongNotFoundException("Song not found"));

        return RoomQueueItemDto.builder()
                .queueItemId(item.getId())
                .songId(song.getId())
                .title(song.getTitle())
                .artist(song.getArtist())
                .coverImage(song.getCoverImage())
                .duration(song.getDuration())
                .hlsUrl(song.getHlsUrl())
                .addedBy(item.getAddedBy())
                .orderIndex(item.getOrderIndex())
                .sourceType(item.getSourceType())
                .sourceId(item.getSourceId())
                .build();
    }
}
