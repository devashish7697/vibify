package com.vibify.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic WebSocket command payload used by all room commands.
 *
 * This DTO is intentionally flexible to support
 * multiple command types without requiring many small classes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WsCommand {

    /*
     * Playback commands
     */

    private Long songId;

    private Long playlistId;

    private Long offsetMillis;

    /*
     * Queue commands
     */

    private Long queueItemId;

    private Integer newPosition;

    /*
     * Room commands (future support)
     */

    private String inviteCode;
}