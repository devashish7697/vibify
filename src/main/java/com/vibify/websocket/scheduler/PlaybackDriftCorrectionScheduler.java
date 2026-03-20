package com.vibify.websocket.scheduler;

import com.vibify.room.dto.RoomPlaybackStateDto;
import com.vibify.room.model.playback_state.PlaybackStatus;
import com.vibify.room.model.playback_state.RoomPlaybackState;
import com.vibify.room.repository.RoomPlaybackStateRepository;
import com.vibify.room.service.RoomPlaybackService;
import com.vibify.websocket.dto.WsEvent;
import com.vibify.websocket.event.RoomEventPublisher;
import com.vibify.websocket.event.RoomEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlaybackDriftCorrectionScheduler {

    private final RoomPlaybackStateRepository playbackRepository;
    private final RoomEventPublisher eventPublisher;

    /**
     * Runs every 20 seconds to correct playback drift across clients.
     */
    @Scheduled(fixedRate = 20000)
    public void broadcastPlaybackSync() {

        List<RoomPlaybackState> playingRooms =
                playbackRepository.findByStatus(PlaybackStatus.PLAYING);

        if (playingRooms.isEmpty()) {
            return;
        }

        for (RoomPlaybackState state : playingRooms) {

            try {

                if (state.getStartedAt() == null) {
                    continue;
                }

                RoomPlaybackStateDto dto = mapToDto(state);

                WsEvent<RoomPlaybackStateDto> event =
                        WsEvent.<RoomPlaybackStateDto>builder()
                                .eventType(RoomEventType.PLAYBACK_STATE_SYNC)
                                .roomId(state.getRoomId())
                                .timestamp(System.currentTimeMillis())
                                .data(dto)
                                .build();

                eventPublisher.publishPlaybackEvent(
                        state.getRoomId(),
                        event
                );

            } catch (Exception ex) {

                log.error("[DRIFT SYNC] Failed for room {}",
                        state.getRoomId(),
                        ex);
            }
        }
    }

    private RoomPlaybackStateDto mapToDto(RoomPlaybackState state) {

        return RoomPlaybackStateDto.builder()
                .roomId(state.getRoomId())
                .songId(state.getSongId())
                .queueItemId(state.getQueueItemId())
                .offsetMillis(state.getOffsetMillis())
                .status(state.getStatus())
                .version(state.getStateVersion())
                .serverTime(System.currentTimeMillis())
                .startedAt(state.getStartedAt())
                .updatedAt(state.getUpdatedAt())
                .build();
    }
}