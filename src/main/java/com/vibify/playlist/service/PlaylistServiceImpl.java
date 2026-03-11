package com.vibify.playlist.service;

import com.vibify.playlist.dto.*;
import com.vibify.common.exception.*;
import com.vibify.playlist.model.*;
import com.vibify.playlist.repository.*;
import com.vibify.songs.model.Song;
import com.vibify.songs.repository.SongRepository;
import com.vibify.user.model.User;
import com.vibify.user.repository.UserRepository;
import com.vibify.auth.security.SecurityUtils;
import com.vibify.common.exception.SongNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistItemRepository playlistItemRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;

    private static final long ORDER_INCREMENT = 1000L;

    /**
     * Create playlist
     */
    @Override
    @Transactional
    public PlaylistResponse createPlaylist(CreatePlaylistRequest request) {

        Long userId = SecurityUtils.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Playlist playlist = Playlist.builder()
                .name(request.getName())
                .description(request.getDescription())
                .user(user)
                .songCount(0)
                .build();

        Playlist saved = playlistRepository.save(playlist);

        log.info("Playlist created: {}", saved.getId());

        return PlaylistResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .coverImage(saved.getCoverImage())
                .songCount(saved.getSongCount())
                .build();
    }

    /**
     * Get playlists of current user
     */
    @Override
    public List<PlaylistResponse> getUserPlaylists() {

        Long userId = SecurityUtils.getCurrentUserId();

        List<Playlist> playlists =
                playlistRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return playlists.stream()
                .map(p -> PlaylistResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .coverImage(p.getCoverImage())
                        .songCount(p.getSongCount())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get playlist details
     */
    @Override
    public PlaylistDetailResponse getPlaylistDetails(Long playlistId) {

        Long userId = SecurityUtils.getCurrentUserId();

        Playlist playlist = playlistRepository
                .findByIdAndUserId(playlistId, userId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist not found"));

        List<PlaylistItem> items =
                playlistItemRepository.findByPlaylistIdOrderByOrderIndexAsc(playlistId);

        List<PlaylistSongDto> songs = items.stream()
                .map(item -> PlaylistSongDto.builder()
                        .songId(item.getSong().getId())
                        .title(item.getSong().getTitle())
                        .artist(item.getSong().getArtist())
                        .coverImage(item.getSong().getCoverImage())
                        .hlsUrl(item.getSong().getHlsUrl())
                        .duration(item.getSong().getDuration())
                        .orderIndex(item.getOrderIndex())
                        .build())
                .collect(Collectors.toList());

        return PlaylistDetailResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .coverImage(playlist.getCoverImage())
                .songCount(playlist.getSongCount())
                .songs(songs)
                .build();
    }

    /**
     * Delete playlist
     */
    @Override
    @Transactional
    public void deletePlaylist(Long playlistId) {

        Long userId = SecurityUtils.getCurrentUserId();

        Playlist playlist = playlistRepository
                .findByIdAndUserId(playlistId, userId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist not found"));

        playlistRepository.delete(playlist);

        log.info("Playlist deleted {}", playlistId);
    }

    /**
     * Add song to playlist
     */
    @Override
    @Transactional
    public void addSongToPlaylist(Long playlistId, AddSongRequest request) {

        Long userId = SecurityUtils.getCurrentUserId();

        Playlist playlist = playlistRepository
                .findByIdAndUserId(playlistId, userId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist not found"));

        Song song = songRepository.findById(request.getSongId())
                .orElseThrow(() -> new SongNotFoundException("Song not found"));

        playlistItemRepository
                .findByPlaylistIdAndSongId(playlistId, song.getId())
                .ifPresent(item -> {
                    throw new SongAlreadyExistsInPlaylistException(
                            "Song already exists in playlist");
                });

        Long lastOrder = playlistItemRepository
                .findTopByPlaylistIdOrderByOrderIndexDesc(playlistId)
                .map(PlaylistItem::getOrderIndex)
                .orElse(0L);

        Long newOrder = lastOrder + ORDER_INCREMENT;

        PlaylistItem item = PlaylistItem.builder()
                .playlist(playlist)
                .song(song)
                .orderIndex(newOrder)
                .build();

        playlistItemRepository.save(item);

        playlist.setSongCount(playlist.getSongCount() + 1);

        if (playlist.getCoverImage() == null) {
            playlist.setCoverImage(song.getCoverImage());
        }

        playlistRepository.save(playlist);

        log.info("Song {} added to playlist {}", song.getId(), playlistId);
    }

    /**
     * Remove song from playlist
     */
    @Override
    @Transactional
    public void removeSongFromPlaylist(Long playlistId, Long songId) {

        Long userId = SecurityUtils.getCurrentUserId();

        Playlist playlist = playlistRepository
                .findByIdAndUserId(playlistId, userId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist not found"));

        PlaylistItem item = playlistItemRepository
                .findByPlaylistIdAndSongId(playlistId, songId)
                .orElseThrow(() ->
                        new SongNotInPlaylistException("Song not found in playlist"));

        playlistItemRepository.delete(item);

        playlist.setSongCount(playlist.getSongCount() - 1);

        playlistRepository.save(playlist);

        log.info("Song {} removed from playlist {}", songId, playlistId);
    }

    /**
     * Reorder playlist songs
     */
    @Override
    @Transactional
    public void reorderPlaylistSongs(Long playlistId, ReorderPlaylistRequest request) {

        Long userId = SecurityUtils.getCurrentUserId();

        playlistRepository.findByIdAndUserId(playlistId, userId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist not found"));

        List<PlaylistItem> items =
                playlistItemRepository.findByPlaylistIdOrderByOrderIndexAsc(playlistId);

        Map<Long, PlaylistItem> itemMap =
                items.stream().collect(Collectors.toMap(
                        i -> i.getSong().getId(),
                        i -> i
                ));

        long order = ORDER_INCREMENT;

        if (request.getSongIds().size() != items.size()) {
            throw new PlaylistException("Invalid reorder request");
        }

        for (Long songId : request.getSongIds()) {

            PlaylistItem item = itemMap.get(songId);

            if (item == null) {
                throw new SongNotInPlaylistException("Song not in playlist");
            }

            item.setOrderIndex(order);
            order += ORDER_INCREMENT;
        }

        playlistItemRepository.saveAll(items);

        log.info("Playlist {} reordered", playlistId);
    }
}