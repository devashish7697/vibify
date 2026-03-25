package com.vibify.chat.scheduler;

import com.vibify.chat.model.ChatMessage;
import com.vibify.chat.repository.ChatMessageRepository;
import com.vibify.common.storage.ObjectStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatMediaCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ChatMediaCleanupScheduler.class);

    private final ChatMessageRepository chatMessageRepository;
    private final ObjectStorageService storageService;

    public ChatMediaCleanupScheduler(
            ChatMessageRepository chatMessageRepository,
            ObjectStorageService storageService
    ) {
        this.chatMessageRepository = chatMessageRepository;
        this.storageService = storageService;
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000) // every 10 minutes
    public void cleanupMedia() {

        List<ChatMessage> messages = chatMessageRepository.findMessagesPendingMediaCleanup();

        if (messages.isEmpty()) {
            return;
        }

        logger.info("Media cleanup started. Count: {}", messages.size());

        for (ChatMessage message : messages) {
            try {

                String mediaUrl = message.getMediaUrl();
                String key = extractKeyFromUrl(mediaUrl);
                storageService.deleteFile(key);
                message.setMediaDeleted(true);

            } catch (Exception e) {
                logger.error("Failed to cleanup media for messageId: {}", message.getId(), e);
            }
        }

        chatMessageRepository.saveAll(messages);
        logger.info("Media cleanup completed.");
    }

    private String extractKeyFromUrl(String mediaUrl) {
        return mediaUrl.substring(mediaUrl.indexOf("chat-media/"));
    }
}