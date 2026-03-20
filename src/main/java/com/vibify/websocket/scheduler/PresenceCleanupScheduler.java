package com.vibify.websocket.scheduler;

import com.vibify.room.model.room_presence.UserRoomPresence;
import com.vibify.room.repository.UserRoomPresenceRepository;
import com.vibify.websocket.event.RoomEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PresenceCleanupScheduler {

    private final UserRoomPresenceRepository presenceRepository;
    private final RoomEventPublisher eventPublisher;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupInactiveUsers(){

        LocalDateTime cutoff =
                LocalDateTime.now().minusSeconds(40);

        List<UserRoomPresence> users =
                presenceRepository.findInactiveUsers(cutoff);

        if (users.isEmpty()) {
            return;
        }

        for (UserRoomPresence p : users) {

            p.setIsActive(false);

            log.debug("[PRESENCE CLEANUP] user {} inactive in room {}",
                    p.getUserId(), p.getRoomId());

            // 🔥 Step 3 will add event here
            eventPublisher.publishMemberLeft(
                    p.getRoomId(),
                    p.getUserId()
            );
        }
    }
}