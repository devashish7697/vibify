package com.vibify.playback.service;

import com.vibify.playback.dto.PlaybackRequest;
import com.vibify.playback.dto.PlaybackResponse;
import com.vibify.room.model.playback_state.PlaybackSourceType;
import com.vibify.room.service.RoomMembershipService;
import com.vibify.room.service.RoomPlaybackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaybackFacadeImpl implements PlaybackFacade {

    private final RoomMembershipService membershipService;
    private final RoomPlaybackService roomPlaybackService;

    // future
    /// private final Solo PlaybackService soloPlaybackService;

    @Override
    public PlaybackResponse play(PlaybackRequest request, Long userId) {

        // 🔥 STEP 1: Detect active room
        UUID roomId = membershipService.getActiveRoomId(userId).orElse(null);

        // 🔥 STEP 2: Route logic
        if (roomId != null) {
            return handleRoomPlayback(roomId, request, userId);
        } else {

            return handleSoloPlayback(request, userId);
        }
    }

    /**
     * ROOM PLAYBACK FLOW
     */
    private PlaybackResponse handleRoomPlayback(UUID roomId, PlaybackRequest request, Long userId) {

        if (request.getSourceType() == PlaybackSourceType.PLAYLIST) {

            if (request.getPlaylistId() == null) {
                throw new IllegalArgumentException("playlistId is required for playlist playback");
            }

            return PlaybackResponse.builder()
                    .mode("ROOM")
                    .data(roomPlaybackService.playPlaylist(roomId, request.getPlaylistId(), userId))
                    .build();
        }

        if (request.getSongId() == null) {
            throw new IllegalArgumentException("songId is required for song playback");
        }

        // Default → SONG
        return PlaybackResponse.builder()
                .mode("ROOM")
                .data(roomPlaybackService.playSong(roomId, request.getSongId(), userId))
                .build();
    }

    /**
     * SOLO PLAYBACK FLOW (placeholder)
     */
    private PlaybackResponse handleSoloPlayback(PlaybackRequest request, Long userId) {

        // 🔴 Not implemented yet
        throw new UnsupportedOperationException("Solo playback not implemented yet");
    }
}
