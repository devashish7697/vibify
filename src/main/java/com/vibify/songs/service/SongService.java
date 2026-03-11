package com.vibify.songs.service;

import com.vibify.common.globalResponse.PaginatedResponse;
import com.vibify.songs.dto.SongResponseDto;
import org.springframework.data.domain.Page;

public interface SongService {

    PaginatedResponse<SongResponseDto> getSongs(int page, int size);

    PaginatedResponse<SongResponseDto> searchSongs(String query, int page, int size);

    SongResponseDto getSongById(Long id);

}
