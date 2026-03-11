package com.vibify.songs.service;

import com.vibify.common.exception.SongNotFoundException;
import com.vibify.common.globalResponse.PaginatedResponse;
import com.vibify.songs.dto.SongResponseDto;
import com.vibify.songs.model.Song;
import com.vibify.songs.model.SongStatus;
import com.vibify.songs.repository.SongRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;

    public SongServiceImpl(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    @Override
    public PaginatedResponse<SongResponseDto> getSongs(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Song> songs = songRepository.findByStatus(SongStatus.READY, pageable);
        return mapToPaginatedResponse(songs);

    }

    @Override
    public PaginatedResponse<SongResponseDto> searchSongs(String query, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Song> songs =
                songRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCaseAndStatus(
                        query,
                        query,
                        SongStatus.READY,
                        pageable
                );
        return mapToPaginatedResponse(songs);
    }

    @Override
    public SongResponseDto getSongById(Long id) {

        Song song = songRepository.findById(id)
                .filter(s -> s.getStatus() == SongStatus.READY)
                .orElseThrow(() -> new SongNotFoundException("Song not found"));

        return mapToDto(song);
    }

    private PaginatedResponse<SongResponseDto> mapToPaginatedResponse(Page<Song> songs) {

        List<SongResponseDto> content =
                songs.getContent()
                        .stream()
                        .map(this::mapToDto)
                        .toList();

        return PaginatedResponse.<SongResponseDto>builder()
                .content(content)
                .page(songs.getNumber())
                .size(songs.getSize())
                .totalPages(songs.getTotalPages())
                .totalElements(songs.getTotalElements())
                .build();
    }

    private SongResponseDto mapToDto(Song song) {

        return SongResponseDto.builder()
                .id(song.getId())
                .title(song.getTitle())
                .artist(song.getArtist())
                .duration(song.getDuration())
                .genre(song.getGenre())
                .coverImage(song.getCoverImage())
                .hlsUrl(song.getHlsUrl())
                .fileSize(song.getFileSize())
                .build();
    }
}
