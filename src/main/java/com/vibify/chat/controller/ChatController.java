package com.vibify.chat.controller;

import com.vibify.chat.model.ChatMediaType;
import com.vibify.chat.service.ChatMediaService;
import com.vibify.common.globalResponse.GlobalApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/chat/media")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatMediaService chatMediaService;

    public ChatController(ChatMediaService chatMediaService) {
        this.chatMediaService = chatMediaService;
    }

    @PostMapping("/upload")
    public ResponseEntity<GlobalApiResponse<String>> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam("roomId") UUID roomId,
            Authentication authentication
    ) {

        // 🔐 Extract username from JWT principal
        String username = authentication.getName();

        logger.info("Media upload request received. User: {}, Room: {}", username, roomId);

        // 🔴 Safe enum parsing (avoid 500 error)
        ChatMediaType mediaType;
        try {
            mediaType = ChatMediaType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    GlobalApiResponse.error("Invalid media type", "INVALID_MEDIA_TYPE")
            );
        }

        String mediaUrl = chatMediaService.uploadMedia(
                file,
                mediaType,
                roomId,
                username
        );

        return ResponseEntity.ok(
                GlobalApiResponse.success("Media uploaded successfully", mediaUrl)
        );
    }
}
