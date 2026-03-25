package com.vibify.chat.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingEventRequest {

    private Boolean typing; // true = start, false = stop
}