package com.vibify.playlist.controller;

import com.vibify.playlist.dto.*;
import com.vibify.playlist.service.PlaylistService;
import com.vibify.common.globalResponse.GlobalApiResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    /**
     * Create playlist
     */
    @PostMapping("/create")
    public GlobalApiResponse<PlaylistResponse> createPlaylist(
            @Valid @RequestBody CreatePlaylistRequest request) {

        log.info("Create playlist request received");
        PlaylistResponse response = playlistService.createPlaylist(request);

        return GlobalApiResponse.success("Playlist created successfully", response );
    }

    /**
     * Get user's playlists
     */
    @GetMapping("/my-playlists")
    public GlobalApiResponse<List<PlaylistResponse>> getMyPlaylists() {

        List<PlaylistResponse> playlists = playlistService.getUserPlaylists();

        return GlobalApiResponse.success( "Playlists fetched successfully", playlists);
    }

    /**
     * Get playlist details
     */
    @GetMapping("/{playlistId}/details")
    public GlobalApiResponse<PlaylistDetailResponse> getPlaylistDetails(
            @PathVariable Long playlistId) {

        PlaylistDetailResponse playlist =
                playlistService.getPlaylistDetails(playlistId);

        return GlobalApiResponse.success("Playlist fetched successfully", playlist);
    }

    /**
     * Delete playlist
     */
    @DeleteMapping("/{playlistId}/delete")
    public GlobalApiResponse<Void> deletePlaylist(@PathVariable Long playlistId) {

        playlistService.deletePlaylist(playlistId);

        return GlobalApiResponse.success( "Playlist deleted successfully", null);
    }

    /**
     * Add song to playlist
     */
    @PostMapping("/{playlistId}/songs/add")
    public GlobalApiResponse<Void> addSongToPlaylist(
            @PathVariable Long playlistId,
            @Valid @RequestBody AddSongRequest request) {

        playlistService.addSongToPlaylist(playlistId, request);

        return GlobalApiResponse.success( "Song added to playlist", null);
    }

    /**
     * Remove song from playlist
     */
    @DeleteMapping("/{playlistId}/songs/{songId}/remove")
    public GlobalApiResponse<Void> removeSongFromPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long songId) {

        playlistService.removeSongFromPlaylist(playlistId, songId);

        return GlobalApiResponse.success("Song removed from playlist", null);
    }

    /**
     * Reorder playlist songs
     */
    @PutMapping("/{playlistId}/songs/reorder")
    public GlobalApiResponse<Void> reorderSongs(
            @PathVariable Long playlistId,
            @Valid @RequestBody ReorderPlaylistRequest request) {

        playlistService.reorderPlaylistSongs(playlistId, request);

        return GlobalApiResponse.success("Playlist reordered successfully", null);
    }
}
