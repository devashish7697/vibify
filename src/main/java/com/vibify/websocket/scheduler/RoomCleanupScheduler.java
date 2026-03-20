package com.vibify.websocket.scheduler;

import com.vibify.room.model.room_entity.Room;
import com.vibify.room.model.room_entity.RoomStatus;
import com.vibify.room.repository.RoomRepository;
import com.vibify.room.repository.UserRoomPresenceRepository;
import com.vibify.websocket.event.RoomEventPublisher;
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
public class RoomCleanupScheduler {

    private final RoomRepository roomRepository;
    private final UserRoomPresenceRepository presenceRepository;
    private final RoomEventPublisher eventPublisher;

    private static final long ROOM_TIMEOUT = 10 * 60 * 1000; // 10 minutes

    @Scheduled(fixedRate = 300000) // run every 5 minutes
    @Transactional
    public void cleanupInactiveRooms() {

        LocalDateTime cutoff =
                LocalDateTime.now().minusMinutes(10);

        List<Room> inactiveRooms =
                roomRepository.findInactiveRooms(cutoff);

        if (inactiveRooms.isEmpty()) {
            return;
        }

        for (Room room : inactiveRooms) {

            try {

                if (room.getStatus() == RoomStatus.ENDED) {
                    continue; // idempotency
                }

                if (room.getLastActivityAt() != null &&
                        room.getLastActivityAt().isAfter(cutoff)) {
                    continue;
                }

                long activeUsers =
                        presenceRepository.countActiveUsers(room.getId());

                // Do NOT end if users exist
                if (activeUsers > 0) {
                    continue;
                }

                // activity safety
                if (room.getLastActivityAt() != null &&
                        room.getLastActivityAt().isAfter(cutoff)) {
                    continue;
                }


                room.setStatus(RoomStatus.ENDED);
                room.setEndedAt(LocalDateTime.now());

                log.info("[ROOM CLEANUP] Room {} ended | users=0 | cutoff={}",
                        room.getId(), cutoff);

                eventPublisher.publishRoomEnded(room.getId());

            } catch (Exception ex) {

                log.error("Room cleanup failed for room {}", room.getId(), ex);
            }
        }
    }
}