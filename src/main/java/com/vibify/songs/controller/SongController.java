package com.vibify.songs.controller;

import com.vibify.common.globalResponse.GlobalApiResponse;
import com.vibify.common.globalResponse.PaginatedResponse;
import com.vibify.songs.dto.SongResponseDto;
import com.vibify.songs.service.SongService;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @GetMapping
    public GlobalApiResponse<PaginatedResponse<SongResponseDto>> getSongs(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {

        PaginatedResponse<SongResponseDto> songs = songService.getSongs(page, size);

        return GlobalApiResponse.success(
                "Songs fetched successfully",
                songs
        );
    }

    @GetMapping("/{id}")
    public GlobalApiResponse<SongResponseDto> getSongById(
            @PathVariable Long id
    ) {

        SongResponseDto song = songService.getSongById(id);

        return GlobalApiResponse.success(
                "Song fetched successfully",
                song
        );
    }

    @GetMapping("/search")
    public GlobalApiResponse<PaginatedResponse<SongResponseDto>> searchSongs(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {

        PaginatedResponse<SongResponseDto> songs = songService.searchSongs(query, page, size);

        return GlobalApiResponse.success(
                "Songs fetched successfully",
                songs
        );
    }
}
