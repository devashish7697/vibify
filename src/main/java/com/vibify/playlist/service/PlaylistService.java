package com.vibify.playlist.service;

import com.vibify.playlist.dto.*;

import java.util.List;

public interface PlaylistService {

    PlaylistResponse createPlaylist(CreatePlaylistRequest request);

    List<PlaylistResponse> getUserPlaylists();

    PlaylistDetailResponse getPlaylistDetails(Long playlistId);

    void deletePlaylist(Long playlistId);

    void addSongToPlaylist(Long playlistId, AddSongRequest request);

    void removeSongFromPlaylist(Long playlistId, Long songId);

    void reorderPlaylistSongs(Long playlistId, ReorderPlaylistRequest request);

}