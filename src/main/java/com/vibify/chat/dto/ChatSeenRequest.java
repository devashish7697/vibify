package com.vibify.chat.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSeenRequest {

    private Long lastSeenMessageId;
}