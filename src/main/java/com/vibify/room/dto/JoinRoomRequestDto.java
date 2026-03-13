package com.vibify.room.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinRoomRequestDto {

    @NotNull(message = "invitation code is required")
    private String inviteCode;

}