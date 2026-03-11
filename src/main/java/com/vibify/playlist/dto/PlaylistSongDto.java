package com.vibify.playlist.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistSongDto {

    private Long songId;

    private String title;

    private String artist;

    private String coverImage;

    private String hlsUrl;

    private Integer duration;

    private Long orderIndex;

}