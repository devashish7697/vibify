package com.vibify.room.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponseDto {

    private UUID roomId;

    private String name;

    private String inviteCode;

    private Long hostUserId;

    private boolean isPrivate;

    private String status;

}