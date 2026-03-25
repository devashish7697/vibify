package com.vibify.websocket.scheduler;

import com.vibify.chat.repository.ChatMessageRepository;
import com.vibify.common.storage.ObjectStorageService;
import com.vibify.room.model.room_entity.Room;
import com.vibify.room.model.room_entity.RoomStatus;
import com.vibify.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomDeletionScheduler {

    private final RoomRepository roomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ObjectStorageService storageService;

    @Scheduled(fixedRate = 300000) // every 5 min
    @Transactional
    public void deleteOldRooms() {

        LocalDateTime cutoff =
                LocalDateTime.now().minusMinutes(15);

        List<Room> rooms =
                roomRepository.findRoomsToDelete(cutoff);

        if (rooms.isEmpty()) {
            return;
        }

        for (Room room : rooms) {

            try {

                if (room.getStatus() != RoomStatus.ENDED) {
                    continue;
                }

                log.info("[ROOM DELETE] Room {} deleted | endedAt={}",
                        room.getId(), room.getEndedAt());

                // 🔥 STEP 1: DELETE MEDIA
                List<String> mediaUrls =
                        chatMessageRepository.findMediaUrlsByRoomId(room.getId());

                for (String url : mediaUrls) {
                    try {
                        String key = extractKeyFromUrl(url);
                        storageService.deleteFile(key);
                    } catch (Exception e) {
                        log.error("Failed to delete media: {}", url, e);
                    }
                }

                roomRepository.delete(room);

                log.info("[ROOM DELETE] Room {} fully deleted", room.getId());

            } catch (Exception e) {

                log.error("Room deletion failed for room {}", room.getId(), e);

            }

        }
    }

    private String extractKeyFromUrl(String mediaUrl) {
        return mediaUrl.substring(mediaUrl.indexOf("chat-media/"));
    }
}