package com.vibify.room.controller;

import com.vibify.common.globalResponse.GlobalApiResponse;
import com.vibify.auth.security.SecurityUtils;
import com.vibify.room.dto.ReorderQueueRequestDto;
import com.vibify.room.dto.*;
import com.vibify.room.service.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final RoomMembershipService membershipService;
    private final RoomQueueService queueService;
    private final RoomPlaybackService playbackService;
    private final RoomStateService roomStateService;

    /*
     ------------------------------------------------
     ROOM MANAGEMENT
     ------------------------------------------------
     */

    @PostMapping("/create")
    public GlobalApiResponse<RoomResponseDto> createRoom(
            @RequestBody CreateRoomRequestDto request) {

        Long userId = SecurityUtils.getCurrentUserId();

        RoomResponseDto response =
                roomService.createRoom(request, userId);

        return GlobalApiResponse.success( "Room created successfully", response);
    }

    @DeleteMapping("/{roomId}")
    public GlobalApiResponse<Void> deleteRoom(
            @PathVariable UUID roomId) {

        Long userId = SecurityUtils.getCurrentUserId();

        roomService.deleteRoom(roomId, userId);

        return GlobalApiResponse.success( "Room deleted successfully", null);
    }

    @GetMapping("/{roomId}")
    public GlobalApiResponse<RoomResponseDto> getRoom(
            @PathVariable UUID roomId) {

        RoomResponseDto room =
                roomService.getRoomById(roomId);

        return GlobalApiResponse.success(room);
    }

    @GetMapping("/invite/{inviteCode}")
    public GlobalApiResponse<RoomResponseDto> getRoomByInviteCode(
            @PathVariable String inviteCode) {

        RoomResponseDto room =
                roomService.getRoomByInviteCode(inviteCode);

        return GlobalApiResponse.success(room);
    }


    /*
     ------------------------------------------------
     MEMBERSHIP
     ------------------------------------------------
     */

    @PostMapping("/join")
    public GlobalApiResponse<Void> joinRoom(
            @RequestBody JoinRoomRequestDto request) {

        Long userId = SecurityUtils.getCurrentUserId();
        membershipService.joinRoom(request.getInviteCode(), userId);
        return GlobalApiResponse.success( "Joined room successfully", null);
    }

    @PostMapping("/{roomId}/leave")
    public GlobalApiResponse<Void> leaveRoom(
            @PathVariable UUID roomId) {

        Long userId = SecurityUtils.getCurrentUserId();

        membershipService.leaveRoom(roomId, userId);

        return GlobalApiResponse.success( "Left room successfully", null);
    }

    @GetMapping("/{roomId}/members")
    public GlobalApiResponse<List<RoomMemberDto>> getRoomMembers(
            @PathVariable UUID roomId) {

        List<RoomMemberDto> members =
                membershipService.getRoomMembers(roomId);

        return GlobalApiResponse.success(members);
    }


    /*
     ------------------------------------------------
     QUEUE MANAGEMENT
     ------------------------------------------------
     */

    @PostMapping("/{roomId}/queue/add")
    public GlobalApiResponse<RoomQueueItemDto> addSongToQueue(
            @PathVariable UUID roomId,
            @RequestParam Long songId) {

        Long userId = SecurityUtils.getCurrentUserId();

        RoomQueueItemDto item =
                queueService.addSongToQueue(roomId, songId, userId);

        return GlobalApiResponse.success( "Song added to queue", item);
    }

    @DeleteMapping("/{roomId}/queue/{queueItemId}")
    public GlobalApiResponse<Void> removeQueueItem(
            @PathVariable UUID roomId,
            @PathVariable Long queueItemId) {

        Long userId = SecurityUtils.getCurrentUserId();

        queueService.removeSongFromQueue(roomId, queueItemId, userId);

        return GlobalApiResponse.success( "Queue item removed", null);
    }

    @PutMapping("/{roomId}/queue/reorder")
    public GlobalApiResponse<Void> reorderQueueItem(
            @PathVariable UUID roomId,
            @Valid @RequestBody ReorderQueueRequestDto request) {

        Long userId = SecurityUtils.getCurrentUserId();

        queueService.reorderQueueItem(
                roomId,
                request.getQueueItemId(),
                request.getNewOrderIndex(),
                userId
        );

        return GlobalApiResponse.success( "Queue reordered", null);
    }

    @GetMapping("/{roomId}/queue")
    public GlobalApiResponse<List<RoomQueueItemDto>> getRoomQueue(
            @PathVariable UUID roomId) {

        List<RoomQueueItemDto> queue =
                queueService.getRoomQueue(roomId);

        return GlobalApiResponse.success(queue);
    }


    /*
     ------------------------------------------------
     PLAYBACK CONTROL
     ------------------------------------------------
     */

    @PostMapping("/{roomId}/play/song")
    public GlobalApiResponse<RoomPlaybackStateDto> playSong(
            @PathVariable UUID roomId,
            @RequestParam Long songId) {

        Long userId = SecurityUtils.getCurrentUserId();

        RoomPlaybackStateDto playback =
                playbackService.playSong(roomId, songId, userId);

        return GlobalApiResponse.success( "Song playback started", playback);
    }

    @PostMapping("/{roomId}/play/playlist")
    public GlobalApiResponse<RoomPlaybackStateDto> playPlaylist(
            @PathVariable UUID roomId,
            @RequestParam Long playlistId) {

        Long userId = SecurityUtils.getCurrentUserId();

        RoomPlaybackStateDto playback =
                playbackService.playPlaylist(roomId, playlistId, userId);

        return GlobalApiResponse.success( "Playlist playback started", playback);
    }

    @PostMapping("/{roomId}/pause")
    public GlobalApiResponse<RoomPlaybackStateDto> pausePlayback(
            @PathVariable UUID roomId) {

        Long userId = SecurityUtils.getCurrentUserId();

        RoomPlaybackStateDto playback =
                playbackService.pausePlayback(roomId, userId);

        return GlobalApiResponse.success( "Playback paused", playback);
    }

    @PostMapping("/{roomId}/resume")
    public GlobalApiResponse<RoomPlaybackStateDto> resumePlayback(
            @PathVariable UUID roomId) {

        Long userId = SecurityUtils.getCurrentUserId();

        RoomPlaybackStateDto playback =
                playbackService.resumePlayback(roomId, userId);

        return GlobalApiResponse.success( "Playback resumed", playback);
    }

    @PostMapping("/{roomId}/seek")
    public GlobalApiResponse<RoomPlaybackStateDto> seekPlayback(
            @PathVariable UUID roomId,
            @RequestParam Long offsetMillis) {

        Long userId = SecurityUtils.getCurrentUserId();

        RoomPlaybackStateDto playback =
                playbackService.seekPlayback(roomId, offsetMillis, userId);

        return GlobalApiResponse.success( "Playback position updated", playback);
    }

    @PostMapping("/{roomId}/skip")
    public GlobalApiResponse<RoomPlaybackStateDto> skipSong(
            @PathVariable UUID roomId) {

        Long userId = SecurityUtils.getCurrentUserId();

        RoomPlaybackStateDto playback =
                playbackService.skipToNext(roomId, userId);

        return GlobalApiResponse.success( "Skipped to next song", playback);
    }

    @GetMapping("/{roomId}/playback")
    public GlobalApiResponse<RoomPlaybackStateDto> getPlaybackState(
            @PathVariable UUID roomId) {

        RoomPlaybackStateDto playback =
                playbackService.getPlaybackState(roomId);

        return GlobalApiResponse.success(playback);
    }


    /*
     ------------------------------------------------
     ROOM STATE (MAIN ROOM SCREEN API)
     ------------------------------------------------
     */

    @GetMapping("/{roomId}/state")
    public GlobalApiResponse<RoomStateResponseDto> getRoomState(
            @PathVariable UUID roomId) {

        Long userId = SecurityUtils.getCurrentUserId();

        RoomStateResponseDto state =
                roomStateService.getRoomState(roomId, userId);

        return GlobalApiResponse.success(state);
    }
}
