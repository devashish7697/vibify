package com.vibify.notification.client;

import com.vibify.common.exception.InvalidFcmTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LoggingPushClient implements PushClient {

    private static final Logger logger = LoggerFactory.getLogger(LoggingPushClient.class);

    @Override
    public void send(String token, Map<String, Object> payload) {

        if (token.contains("invalid")) {
            throw new InvalidFcmTokenException("Simulated invalid token");
        }

        logger.info("PUSH_EVENT_SENT -> token={}, payload={}", token, payload);

    }
}