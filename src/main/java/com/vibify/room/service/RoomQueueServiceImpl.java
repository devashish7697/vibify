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
    private final RoomService roomService;

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
        roomService.updateRoomActivity(roomId);

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
        roomService.updateRoomActivity(roomId);
    }

    /**
     * Reorder queue item using sparse ordering.
     */
    @Override
    @Transactional
    public void reorderQueueItem(UUID roomId,
                                 Long queueItemId,
                                 Integer newPosition,
                                 Long userId) {

        roomMembershipService.validateUserMembership(roomId, userId);

        List<RoomQueueItem> queue =
                roomQueueItemRepository.findByRoomIdOrderByOrderIndexAsc(roomId);

        RoomQueueItem item = queue.stream()
                .filter(q -> q.getId().equals(queueItemId))
                .findFirst()
                .orElseThrow(() -> new QueueItemNotFoundException("Queue item not found"));

        queue.remove(item);

        if (newPosition < 0 || newPosition > queue.size()) {
            throw new IllegalArgumentException("Invalid queue position");
        }

        queue.add(newPosition, item);

        Integer newOrderIndex;

        if (newPosition == 0) {

            Integer nextIndex = queue.get(1).getOrderIndex();
            newOrderIndex = nextIndex / 2;

        } else if (newPosition == queue.size() - 1) {

            Integer prevIndex = queue.get(queue.size() - 2).getOrderIndex();
            newOrderIndex = prevIndex + ORDER_GAP;

        } else {

            Integer prevIndex = queue.get(newPosition - 1).getOrderIndex();
            Integer nextIndex = queue.get(newPosition + 1).getOrderIndex();

            newOrderIndex = (prevIndex + nextIndex) / 2;
        }

        item.setOrderIndex(newOrderIndex);

        roomQueueItemRepository.save(item);
        roomService.updateRoomActivity(roomId);
    }

    /**
     * Fetch full room queue.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RoomQueueItemDto> getRoomQueue(UUID roomId, Long userId) {

        roomMembershipService.validateUserMembership(roomId, userId);

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
        roomService.updateRoomActivity(roomId);
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
