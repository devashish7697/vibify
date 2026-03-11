package com.vibify.songs.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongResponseDto {

    private Long id;

    private String title;

    private String artist;

    private Integer duration;

    private String genre;

    private String coverImage;

    private String hlsUrl;

    private Long fileSize;
}
