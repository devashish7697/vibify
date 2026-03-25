package com.vibify.chat.service;

import com.vibify.chat.model.ChatMediaType;
import com.vibify.chat.service.ChatMediaService;
import com.vibify.common.storage.ObjectStorageService;
import com.vibify.room.repository.RoomMemberRepository;
import com.vibify.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ChatMediaServiceImpl implements ChatMediaService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMediaServiceImpl.class);

    private static final long MAX_FILE_SIZE = 150L * 1024 * 1024; // 150MB

    private final ObjectStorageService storageService;
    private final RoomMemberRepository roomMemberRepository;
    private final UserRepository userRepository;

    public ChatMediaServiceImpl(
            ObjectStorageService storageService,
            RoomMemberRepository roomMemberRepository,
            UserRepository userRepository
    ) {
        this.storageService = storageService;
        this.roomMemberRepository = roomMemberRepository;
        this.userRepository = userRepository;
    }

    @Override
    public String uploadMedia(
            MultipartFile file,
            ChatMediaType mediaType,
            UUID roomId,
            String username
    ) {

        // 🔴 1. BASIC VALIDATION
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File exceeds 150MB limit");
        }

        // 🔴 2. USER VALIDATION
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isMember = roomMemberRepository
                .existsByRoomIdAndUserIdAndLeftAtIsNull(roomId, user.getId());

        if (!isMember) {
            throw new RuntimeException("User is not a member of this room");
        }

        // 🔴 3. CONTENT TYPE VALIDATION
        String contentType = file.getContentType();

        validateContentType(contentType, mediaType);

        // 🔴 4. EXTENSION EXTRACTION
        String extension = extractExtension(file.getOriginalFilename(), contentType);

        // 🔴 5. GENERATE STORAGE KEY
        String key = generateKey(roomId, extension);

        // 🔴 6. UPLOAD TO R2
        try (InputStream inputStream = file.getInputStream()) {

            String mediaUrl = storageService.uploadFile(
                    inputStream,
                    key,
                    contentType,
                    file.getSize()
            );

            logger.info("Media uploaded successfully. Room: {}, Key: {}", roomId, key);

            return mediaUrl;

        } catch (Exception e) {
            logger.error("Media upload failed for room: {}", roomId, e);
            throw new RuntimeException("Media upload failed", e);
        }
    }

    // =========================
    // 🔧 VALIDATION METHODS
    // =========================

    private void validateContentType(String contentType, ChatMediaType mediaType) {

        if (contentType == null) {
            throw new RuntimeException("Invalid content type");
        }

        Map<ChatMediaType, Set<String>> allowedTypes = Map.of(
                ChatMediaType.IMAGE, Set.of("image/jpeg", "image/png", "image/webp"),
                ChatMediaType.VIDEO, Set.of("video/mp4"),
                ChatMediaType.AUDIO, Set.of("audio/mpeg", "audio/wav")
        );

        Set<String> allowed = allowedTypes.get(mediaType);

        if (allowed == null || !allowed.contains(contentType)) {
            throw new RuntimeException("Invalid file type for " + mediaType);
        }
    }

    private String extractExtension(String filename, String contentType) {

        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        }

        // fallback from content type
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "video/mp4" -> "mp4";
            case "audio/mpeg" -> "mp3";
            case "audio/wav" -> "wav";
            default -> throw new RuntimeException("Unsupported file type");
        };
    }

    private String generateKey(UUID roomId, String extension) {

        LocalDate now = LocalDate.now();

        return String.format(
                "chat-media/%s/%d/%02d/%s.%s",
                roomId,
                now.getYear(),
                now.getMonthValue(),
                UUID.randomUUID(),
                extension
        );
    }
}
