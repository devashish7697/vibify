package com.vibify.playlist.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistDetailResponse {

    private Long id;

    private String name;

    private String description;

    private String coverImage;

    private Integer songCount;

    private List<PlaylistSongDto> songs;

}