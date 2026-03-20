package com.vibify.websocket.controller;

import com.vibify.room.dto.*;
import com.vibify.room.model.playback_state.PlaybackStatus;
import com.vibify.room.service.*;
import com.vibify.websocket.dto.WsCommand;
import com.vibify.websocket.dto.WsEvent;
import com.vibify.websocket.event.RoomEventPublisher;
import com.vibify.websocket.event.RoomEventType;
import com.vibify.websocket.security.WebSocketAuthChannelInterceptor.StompPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RoomWebSocketController {

    private final RoomPlaybackService playbackService;
    private final RoomQueueService queueService;
    private final RoomStateService roomStateService;
    private final RoomMembershipService membershipService;

    private final RoomPresenceService presenceService;

    private final RoomEventPublisher eventPublisher;

    /*
     ---------------------------------------------------------
     PLAY SONG
     ---------------------------------------------------------
     */

    @MessageMapping("/room/{roomId}/play-song")
    public void playSong(@DestinationVariable UUID roomId,
                         @Payload(required = false) WsCommand command,
                         Principal principal) {

        validateCommand(command);
        requireField(command.getSongId(), "songId");

        Long userId = extractUserId(principal);

        log.debug("User {} playing song {} in room {}", userId, command.getSongId(), roomId);

        RoomPlaybackStateDto playback =
                playbackService.playSong(roomId, command.getSongId(), userId);

        List<RoomQueueItemDto> queue =
                queueService.getRoomQueue(roomId, userId);

        publishQueueReplaced(roomId, userId, queue);
        publishPlaybackEvent(roomId, userId, playback, RoomEventType.PLAYBACK_SONG_STARTED);
    }

    /*
     ---------------------------------------------------------
     PLAY PLAYLIST
     ---------------------------------------------------------
     */

    @MessageMapping("/room/{roomId}/play-playlist")
    public void playPlaylist(@DestinationVariable UUID roomId,
                             @Payload(required = false) WsCommand command,
                             Principal principal) {

        validateCommand(command);
        requireField(command.getPlaylistId(), "playlistId");

        Long userId = extractUserId(principal);

        log.debug("User {} playing playlist {} in room {}", userId, command.getPlaylistId(), roomId);

        RoomPlaybackStateDto playback =
                playbackService.playPlaylist(roomId, command.getPlaylistId(), userId);

        List<RoomQueueItemDto> queue =
                queueService.getRoomQueue(roomId, userId);

        publishQueueReplaced(roomId, userId, queue);
        publishPlaybackEvent(roomId, userId, playback, RoomEventType.PLAYBACK_PLAYLIST_STARTED);
    }

    /*
     ---------------------------------------------------------
     PAUSE
     ---------------------------------------------------------
     */

    @MessageMapping("/room/{roomId}/pause")
    public void pause(@DestinationVariable UUID roomId,
                      Principal principal) {

        Long userId = extractUserId(principal);

        RoomPlaybackStateDto state =
                playbackService.pausePlayback(roomId, userId);

        publishPlaybackEvent(roomId, userId, state, RoomEventType.PLAYBACK_PAUSED);
    }

    /*
     ---------------------------------------------------------
     RESUME
     ---------------------------------------------------------
     */

    @MessageMapping("/room/{roomId}/resume")
    public void resume(@DestinationVariable UUID roomId,
                       Principal principal) {

        Long userId = extractUserId(principal);

        RoomPlaybackStateDto state =
                playbackService.resumePlayback(roomId, userId);

        publishPlaybackEvent(roomId, userId, state, RoomEventType.PLAYBACK_RESUMED);
    }

    /*
     ---------------------------------------------------------
     SEEK
     ---------------------------------------------------------
     */

    @MessageMapping("/room/{roomId}/seek")
    public void seek(@DestinationVariable UUID roomId,
                     @Payload(required = false) WsCommand command,
                     Principal principal) {

        validateCommand(command);
        requireField(command.getOffsetMillis(), "offsetMillis");

        if (command.getOffsetMillis() < 0) {
            throw new IllegalArgumentException("offsetMillis cannot be negative");
        }

        Long userId = extractUserId(principal);

        RoomPlaybackStateDto state =
                playbackService.seekPlayback(roomId, command.getOffsetMillis(), userId);

        publishPlaybackEvent(roomId, userId, state, RoomEventType.PLAYBACK_SEEKED);
    }

    /*
     ---------------------------------------------------------
     SKIP
     ---------------------------------------------------------
     */

    @MessageMapping("/room/{roomId}/skip")
    public void skip(@DestinationVariable UUID roomId,
                     Principal principal) {

        Long userId = extractUserId(principal);

        RoomPlaybackStateDto state =
                playbackService.skipToNext(roomId, userId);

        if (state.getStatus() == PlaybackStatus.STOPPED) {

            publishPlaybackEvent(roomId, userId, state, RoomEventType.PLAYBACK_STOPPED);
            return;
        }

        publishPlaybackEvent(roomId, userId, state, RoomEventType.PLAYBACK_SKIPPED);
    }

    /*
     ---------------------------------------------------------
     ADD QUEUE
     ---------------------------------------------------------
     */

    @MessageMapping("/room/{roomId}/queue/add")
    public void addQueue(@DestinationVariable UUID roomId,
                         @Payload(required = false) WsCommand command,
                         Principal principal) {

        validateCommand(command);
        requireField(command.getSongId(), "songId");

        Long userId = extractUserId(principal);

        RoomQueueItemDto item =
                queueService.addSongToQueue(roomId, command.getSongId(), userId);

        WsEvent<RoomQueueItemDto> event = WsEvent.<RoomQueueItemDto>builder()
                .eventType(RoomEventType.QUEUE_ITEM_ADDED)
                .roomId(roomId)
                .triggeredBy(userId)
                .timestamp(System.currentTimeMillis())
                .data(item)
                .build();

        eventPublisher.publishQueueEvent(roomId, event);
    }

    /*
     ---------------------------------------------------------
     REMOVE QUEUE
     ---------------------------------------------------------
     */

    @MessageMapping("/room/{roomId}/queue/remove")
    public void removeQueue(@DestinationVariable UUID roomId,
                            @Payload(required = false) WsCommand command,
                            Principal principal) {

        validateCommand(command);
        requireField(command.getQueueItemId(), "queueItemId");

        Long userId = extractUserId(principal);

        queueService.removeSongFromQueue(roomId, command.getQueueItemId(), userId);

        WsEvent<Long> event = WsEvent.<Long>builder()
                .eventType(RoomEventType.QUEUE_ITEM_REMOVED)
                .roomId(roomId)
                .triggeredBy(userId)
                .timestamp(System.currentTimeMillis())
                .data(command.getQueueItemId())
                .build();

        eventPublisher.publishQueueEvent(roomId, event);
    }

    /*
     ---------------------------------------------------------
     REORDER QUEUE
     ---------------------------------------------------------
     */

    @MessageMapping("/room/{roomId}/queue/reorder")
    public void reorderQueue(@DestinationVariable UUID roomId,
                             @Payload(required = false) WsCommand command,
                             Principal principal) {

        validateCommand(command);
        requireField(command.getQueueItemId(), "queueItemId");
        requireField(command.getNewPosition(), "newPosition");

        if (command.getNewPosition() < 0) {
            throw new IllegalArgumentException("newPosition cannot be negative");
        }

        Long userId = extractUserId(principal);

        queueService.reorderQueueItem(
                roomId,
                command.getQueueItemId(),
                command.getNewPosition(),
                userId
        );

        List<RoomQueueItemDto> queue =
                queueService.getRoomQueue(roomId, userId);

        WsEvent<List<RoomQueueItemDto>> event =
                WsEvent.<List<RoomQueueItemDto>>builder()
                        .eventType(RoomEventType.QUEUE_ITEM_REORDERED)
                        .roomId(roomId)
                        .triggeredBy(userId)
                        .timestamp(System.currentTimeMillis())
                        .data(queue)
                        .build();

        eventPublisher.publishQueueEvent(roomId, event);
    }

    /*
     ---------------------------------------------------------
     ROOM STATE SYNC
     ---------------------------------------------------------
     */

    @MessageMapping("/room/{roomId}/state")
    public void syncState(@DestinationVariable UUID roomId,
                          Principal principal) {

        Long userId = extractUserId(principal);

        RoomStateResponseDto state =
                roomStateService.getRoomState(roomId, userId);

        WsEvent<RoomStateResponseDto> event =
                WsEvent.<RoomStateResponseDto>builder()
                        .eventType(RoomEventType.ROOM_STATE_SYNC)
                        .roomId(roomId)
                        .triggeredBy(userId)
                        .timestamp(System.currentTimeMillis())
                        .version(state.getPlayback().getVersion())
                        .data(state)
                        .build();

        eventPublisher.publishStateEvent(roomId, event);
    }


    /// room presence

    @MessageMapping("/room/{roomId}/presence/join")
    public void joinPresence(@DestinationVariable UUID roomId,
                             Principal principal){

        Long userId = extractUserId(principal);

        presenceService.joinPresence(roomId,userId);
    }

    @MessageMapping("/room/{roomId}/presence/heartbeat")
    public void heartbeat(@DestinationVariable UUID roomId,
                          Principal principal){

        Long userId = extractUserId(principal);

        presenceService.heartbeat(roomId,userId);
    }

    @MessageMapping("/room/{roomId}/presence/leave")
    public void leavePresence(@DestinationVariable UUID roomId,
                              Principal principal){

        Long userId = extractUserId(principal);

        presenceService.leavePresence(roomId,userId);
    }

    /*
     ---------------------------------------------------------
     VALIDATION HELPERS
     ---------------------------------------------------------
     */

    private void validateCommand(WsCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command payload is required");
        }
    }

    private void requireField(Object field, String name) {
        if (field == null) {
            throw new IllegalArgumentException(name + " is required");
        }
    }

    /*
     ---------------------------------------------------------
     EVENT HELPERS
     ---------------------------------------------------------
     */

    private void publishPlaybackEvent(UUID roomId,
                                      Long userId,
                                      RoomPlaybackStateDto state,
                                      RoomEventType type) {

        WsEvent<RoomPlaybackStateDto> event =
                WsEvent.<RoomPlaybackStateDto>builder()
                        .eventType(type)
                        .roomId(roomId)
                        .triggeredBy(userId)
                        .timestamp(System.currentTimeMillis())
                        .version(state.getVersion())
                        .data(state)
                        .build();

        eventPublisher.publishPlaybackEvent(roomId, event);
    }

    private void publishQueueReplaced(UUID roomId,
                                      Long userId,
                                      List<RoomQueueItemDto> queue) {

        WsEvent<List<RoomQueueItemDto>> event =
                WsEvent.<List<RoomQueueItemDto>>builder()
                        .eventType(RoomEventType.QUEUE_REPLACED)
                        .roomId(roomId)
                        .triggeredBy(userId)
                        .timestamp(System.currentTimeMillis())
                        .data(queue)
                        .build();

        eventPublisher.publishQueueEvent(roomId, event);
    }

    /*
     ---------------------------------------------------------
     PRINCIPAL UTILITY
     ---------------------------------------------------------
     */

    private Long extractUserId(Principal principal) {

        if (!(principal instanceof StompPrincipal stompPrincipal)) {
            throw new IllegalStateException("Invalid WebSocket principal");
        }

        return stompPrincipal.getUserId();
    }
}