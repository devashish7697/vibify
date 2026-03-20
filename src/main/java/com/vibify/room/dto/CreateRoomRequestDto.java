package com.vibify.room.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequestDto {

    @NotNull(message = "Name is required")
    private String name;

    @JsonProperty("isPrivate")
    private boolean isPrivate;

}