package com.vibify.chat.service;

import com.vibify.chat.model.ChatMediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ChatMediaService {

    String uploadMedia(
            MultipartFile file,
            ChatMediaType mediaType,
            UUID roomId,
            String username
    );

}
