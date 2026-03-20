package com.vibify.websocket.scheduler;

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

            if (room.getStatus() != RoomStatus.ENDED) {
                continue;
            }

            log.info("[ROOM DELETE] Room {} deleted | endedAt={}",
                    room.getId(), room.getEndedAt());
        }

        roomRepository.deleteAll(rooms);
    }
}