package com.vibify.playback.dto;

import com.vibify.room.model.playback_state.PlaybackSourceType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaybackRequest {

    @NotNull(message = "songId is required")
    private Long songId;

    // future use (playlist, etc.)
    private Long playlistId;

    // extensibility
    private PlaybackSourceType sourceType;
}