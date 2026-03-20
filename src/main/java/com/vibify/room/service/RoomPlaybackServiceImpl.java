package com.vibify.room.service;

import com.vibify.common.exception.PlaylistAccessDeniedException;
import com.vibify.common.exception.PlaylistException;
import com.vibify.common.exception.SongNotFoundException;
import com.vibify.common.exception.room_exception.PlaybackStateNotFoundException;
import com.vibify.playlist.model.Playlist;
import com.vibify.playlist.repository.PlaylistRepository;
import com.vibify.room.dto.RoomPlaybackStateDto;
import com.vibify.room.model.playback_state.PlaybackSourceType;
import com.vibify.room.model.playback_state.PlaybackStatus;
import com.vibify.room.model.playback_state.RoomPlaybackState;
import com.vibify.room.model.room_queue.QueueSourceType;
import com.vibify.room.model.room_queue.RoomQueueItem;
import com.vibify.room.repository.RoomPlaybackStateRepository;
import com.vibify.room.repository.RoomQueueItemRepository;
import com.vibify.songs.model.Song;
import com.vibify.songs.repository.SongRepository;
import com.vibify.playlist.model.PlaylistItem;
import com.vibify.playlist.repository.PlaylistItemRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class RoomPlaybackServiceImpl implements RoomPlaybackService {

    private final RoomPlaybackStateRepository playbackRepository;
    private final RoomQueueItemRepository queueRepository;
    private final RoomQueueService roomQueueService;
    private final RoomMembershipService membershipService;
    private final SongRepository songRepository;
    private final PlaylistItemRepository playlistItemRepository;
    private final RoomPlaybackLockManager lockManager;

    private final PlaylistRepository playlistRepository;
    private final RoomService roomService;

    private static final int ORDER_GAP = 1000;

    /**
     * Play a single song
     */
    @Transactional
    public RoomPlaybackStateDto playSongInternal(UUID roomId, Long songId, Long userId) {

            membershipService.validateUserMembership(roomId, userId);

            Song song = songRepository.findById(songId)
                    .orElseThrow(() -> new SongNotFoundException("Song not found"));

            roomQueueService.clearQueue(roomId);

            RoomQueueItem queueItem = RoomQueueItem.builder()
                    .roomId(roomId)
                    .songId(songId)
                    .addedBy(userId)
                    .orderIndex(ORDER_GAP)
                    .sourceType(QueueSourceType.MANUAL)
                    .sourceId(null)
                    .createdAt(System.currentTimeMillis())
                    .build();

            queueItem = queueRepository.save(queueItem);

            RoomPlaybackState state = createOrUpdatePlaybackState(
                    roomId,
                    queueItem,
                    userId,
                    QueueSourceType.MANUAL,
                    null
            );

            roomService.updateRoomActivity(roomId);

            return mapToDto(state, song);
    }

    @Override
    public RoomPlaybackStateDto playSong(UUID roomId, Long songId, Long userId) {

        ReentrantLock lock = lockManager.getLock(roomId);
        lock.lock();

        try {
           return  playSongInternal(roomId,songId,userId);
        } finally {
            lock.unlock();
            lockManager.releaseLockIfUnused(roomId);
        }
    }

    /**
     * Play playlist
     */

    @Transactional
    public RoomPlaybackStateDto playPlaylistInternal(UUID roomId, Long playlistId, Long userId) {

            membershipService.validateUserMembership(roomId, userId);
            Playlist playlist = playlistRepository.findById(playlistId)
                    .orElseThrow(() -> new PlaylistException("Playlist not found"));


            ///  A User can play his playlist only which he created in his account
            if (!playlist.getUser().getId().equals(userId)) {
                throw new PlaylistAccessDeniedException("You can only play your own playlists in a room");
            }

            roomQueueService.clearQueue(roomId);

            List<PlaylistItem> items =
                    playlistItemRepository.findByPlaylistIdOrderByOrderIndexAsc(playlistId);

            if (items.isEmpty()) {
                throw new PlaylistException("Playlist has no songs");
            }

            int order = ORDER_GAP;
            RoomQueueItem firstQueueItem = null;

            for (PlaylistItem item : items) {

                RoomQueueItem queueItem = RoomQueueItem.builder()
                        .roomId(roomId)
                        .songId(item.getSong().getId())
                        .addedBy(userId)
                        .orderIndex(order)
                        .sourceType(QueueSourceType.PLAYLIST)
                        .sourceId(playlistId)
                        .createdAt(System.currentTimeMillis())
                        .build();

                queueItem = queueRepository.save(queueItem);

                if (firstQueueItem == null) {
                    firstQueueItem = queueItem;
                }

                order += ORDER_GAP;
            }

            Song song = songRepository.findById(firstQueueItem.getSongId())
                    .orElseThrow(() -> new SongNotFoundException("Song not found"));

            RoomPlaybackState state = createOrUpdatePlaybackState(
                    roomId,
                    firstQueueItem,
                    userId,
                    QueueSourceType.PLAYLIST,
                    playlistId
            );

            roomService.updateRoomActivity(roomId);

            return mapToDto(state, song);
    }

    @Override
    public RoomPlaybackStateDto playPlaylist(UUID roomId, Long playlistId, Long userId) {

        ReentrantLock lock = lockManager.getLock(roomId);
        lock.lock();

        try {
            return playPlaylistInternal(roomId,playlistId,userId);
        } finally {
            lock.unlock();
            lockManager.releaseLockIfUnused(roomId);
        }
    }

    /**
     * Pause playback
     */
    @Transactional
    public RoomPlaybackStateDto pausePlaybackInternal(UUID roomId, Long userId) {

            membershipService.validateUserMembership(roomId, userId);

            RoomPlaybackState state = getState(roomId);

            if (state.getStatus() == PlaybackStatus.PAUSED) {
                return mapToDto(state);
            }

            long now = System.currentTimeMillis();

            if (state.getStartedAt() == null) {
                return mapToDto(state);
            }

            long elapsed = now - state.getStartedAt();
            state.setOffsetMillis(state.getOffsetMillis() + elapsed);

            state.setStatus(PlaybackStatus.PAUSED);
            state.setUpdatedAt(now);
            state.setLastActionBy(userId);

            incrementVersion(state);
            playbackRepository.save(state);
            roomService.updateRoomActivity(roomId);

            return mapToDto(state);
    }

    @Override
    public RoomPlaybackStateDto pausePlayback(UUID roomId, Long userId) {

        ReentrantLock lock = lockManager.getLock(roomId);
        lock.lock();

        try {
           return pausePlaybackInternal(roomId,userId);
        } finally {
            lock.unlock();
            lockManager.releaseLockIfUnused(roomId);
        }

    }

    /**
     * Resume playback
     */

    @Transactional
    public RoomPlaybackStateDto resumePlaybackInternal(UUID roomId, Long userId) {

            membershipService.validateUserMembership(roomId, userId);

            RoomPlaybackState state = getState(roomId);

            if (state.getStatus() == PlaybackStatus.PLAYING) {
                return mapToDto(state);
            }

            long now = System.currentTimeMillis();

            state.setStartedAt(now);
            state.setStatus(PlaybackStatus.PLAYING);
            state.setUpdatedAt(now);
            state.setLastActionBy(userId);

            incrementVersion(state);
            playbackRepository.save(state);
            roomService.updateRoomActivity(roomId);

            return mapToDto(state);
    }

    @Override
    public RoomPlaybackStateDto resumePlayback(UUID roomId, Long userId) {

        ReentrantLock lock = lockManager.getLock(roomId);
        lock.lock();

        try {
           return resumePlaybackInternal(roomId,userId);
        } finally {
            lock.unlock();
            lockManager.releaseLockIfUnused(roomId);
        }
    }

    /**
     * Seek playback
     */

    @Transactional
    public RoomPlaybackStateDto seekPlaybackInternal(UUID roomId, Long newOffsetMillis, Long userId) {

            membershipService.validateUserMembership(roomId, userId);

            RoomPlaybackState state = getState(roomId);

            if (state.getStartedAt() == null) {
                state.setOffsetMillis(newOffsetMillis);
                incrementVersion(state);
                playbackRepository.save(state);
                return mapToDto(state);
            }

            long now = System.currentTimeMillis();

            state.setOffsetMillis(newOffsetMillis);
            state.setStartedAt(now);
            state.setUpdatedAt(now);
            state.setLastActionBy(userId);

            incrementVersion(state);
            playbackRepository.save(state);
        roomService.updateRoomActivity(roomId);

            return mapToDto(state);
    }

    @Override
    public RoomPlaybackStateDto seekPlayback(UUID roomId, Long newOffsetMillis, Long userId) {

        ReentrantLock lock = lockManager.getLock(roomId);
        lock.lock();

        try {
           return seekPlaybackInternal(roomId,newOffsetMillis,userId);
        } finally {
            lock.unlock();
            lockManager.releaseLockIfUnused(roomId);
        }
    }

    /**
     * Skip to next
     */
    @Transactional
    public RoomPlaybackStateDto skipToNextInternal(UUID roomId, Long userId) {

            membershipService.validateUserMembership(roomId, userId);

            RoomPlaybackState state = getState(roomId);

            if (state.getQueueItemId() == null) {
                state.setStatus(PlaybackStatus.STOPPED);
                incrementVersion(state);
                playbackRepository.save(state);
                return mapToDto(state);
            }

            RoomQueueItem current = queueRepository.findById(state.getQueueItemId())
                    .orElseThrow(() -> new RuntimeException("Current queue item not found"));

            RoomQueueItem next = queueRepository
                    .findFirstByRoomIdAndOrderIndexGreaterThanOrderByOrderIndexAsc(
                            roomId,
                            current.getOrderIndex()
                    )
                    .orElse(null);

            if (next == null) {
                /// clear old values
                state.setStatus(PlaybackStatus.STOPPED);
                state.setOffsetMillis(0L);
                state.setStartedAt(null);
                state.setUpdatedAt(System.currentTimeMillis());

                incrementVersion(state);
                playbackRepository.save(state);
                roomService.updateRoomActivity(roomId);

                return mapToDto(state);
            }

            Song song = songRepository.findById(next.getSongId())
                    .orElseThrow(() -> new SongNotFoundException("Song not found"));

            RoomPlaybackState newState = createOrUpdatePlaybackState(
                    roomId,
                    next,
                    userId,
                    next.getSourceType(),
                    next.getSourceId()
            );
            roomService.updateRoomActivity(roomId);

            return mapToDto(newState, song);
    }


    @Override
    public RoomPlaybackStateDto skipToNext(UUID roomId, Long userId) {

        ReentrantLock lock = lockManager.getLock(roomId);
        lock.lock();

        try {
            return skipToNextInternal(roomId,userId);
        } finally {
            lock.unlock();
            lockManager.releaseLockIfUnused(roomId);
        }

    }

    /**
     * Get playback state
     */
    @Override
    @Transactional(readOnly = true)
    public RoomPlaybackStateDto getPlaybackState(UUID roomId, Long userId) {

        membershipService.validateUserMembership(roomId, userId);

        RoomPlaybackState state = playbackRepository.findById(roomId)
                .orElse(null);

        if (state == null) {
            return null;
        }

        return mapToDto(state);
    }

    private RoomPlaybackState getState(UUID roomId) {

        return playbackRepository.findById(roomId)
                .orElseThrow(() ->
                        new PlaybackStateNotFoundException("Playback state not found"));
    }

    private RoomPlaybackState createOrUpdatePlaybackState(
            UUID roomId,
            RoomQueueItem queueItem,
            Long userId,
            QueueSourceType sourceType,
            Long sourceId
    ) {

        long now = System.currentTimeMillis();
        PlaybackSourceType playbackSource;

        RoomPlaybackState state = playbackRepository.findById(roomId)
                .orElse(RoomPlaybackState.builder().roomId(roomId).build());

        if (!queueItem.getRoomId().equals(roomId)) {
            throw new RuntimeException("Queue item does not belong to this room");
        }

        switch (sourceType) {
            case PLAYLIST:
                playbackSource = PlaybackSourceType.PLAYLIST;
                break;
            default:
                playbackSource = PlaybackSourceType.SONG;
        }

        state.setSongId(queueItem.getSongId());
        state.setQueueItemId(queueItem.getId());
        state.setSourceType(playbackSource);
        state.setSourceId(sourceId);
        state.setStartedAt(now);
        state.setOffsetMillis(0L);
        state.setStatus(PlaybackStatus.PLAYING);
        state.setLastActionBy(userId);
        state.setUpdatedAt(now);

        incrementVersion(state);
        return playbackRepository.save(state);
    }

    private RoomPlaybackStateDto mapToDto(RoomPlaybackState state) {

        // Room exists but playback never started
        if (state.getSongId() == null) {
            return RoomPlaybackStateDto.builder()
                    .roomId(state.getRoomId())
                    .queueItemId(state.getQueueItemId())
                    .offsetMillis(state.getOffsetMillis())
                    .status(state.getStatus())
                    .version(state.getStateVersion())
                    .serverTime(System.currentTimeMillis())
                    .startedAt(state.getStartedAt())
                    .updatedAt(state.getUpdatedAt())
                    .build();
        }

        Song song = songRepository.findById(state.getSongId())
                .orElseThrow(() -> new SongNotFoundException("Song not found"));

        return mapToDto(state, song);
    }

    private RoomPlaybackStateDto mapToDto(RoomPlaybackState state, Song song) {

        return RoomPlaybackStateDto.builder()
                .roomId(state.getRoomId())
                .songId(song.getId())
                .title(song.getTitle())
                .artist(song.getArtist())
                .coverImage(song.getCoverImage())
                .duration(song.getDuration())
                .hlsUrl(song.getHlsUrl())
                .queueItemId(state.getQueueItemId())
                .offsetMillis(state.getOffsetMillis())
                .status(state.getStatus())
                .version(state.getStateVersion())
                .serverTime(System.currentTimeMillis())
                .startedAt(state.getStartedAt())
                .updatedAt(state.getUpdatedAt())
                .build();
    }

    private void incrementVersion(RoomPlaybackState state) {
        if (state.getStateVersion() == null) {
            state.setStateVersion(1L);
        } else {
            state.setStateVersion(state.getStateVersion() + 1);
        }
    }
}