package com.vibify.room.service;

import com.vibify.room.dto.RoomPlaybackStateDto;

import java.util.UUID;

public interface RoomPlaybackService {

    /**
     * Start playback of a single song inside the room.
     */
    RoomPlaybackStateDto playSong(UUID roomId, Long songId, Long userId);

    /**
     * Start playback of a playlist inside the room.
     * The playlist will populate the room queue.
     */
    RoomPlaybackStateDto playPlaylist(UUID roomId, Long playlistId, Long userId);

    /**
     * Pause playback.
     */
    RoomPlaybackStateDto pausePlayback(UUID roomId, Long userId);

    /**
     * Resume playback from paused state.
     */
    RoomPlaybackStateDto resumePlayback(UUID roomId, Long userId);

    /**
     * Seek playback to a new position.
     */
    RoomPlaybackStateDto seekPlayback(UUID roomId, Long newOffsetMillis, Long userId);

    /**
     * Skip to the next song in the queue.
     */
    RoomPlaybackStateDto skipToNext(UUID roomId, Long userId);

    /**
     * Retrieve current playback state for a room.
     */
    RoomPlaybackStateDto getPlaybackState(UUID roomId, Long userId );

}
