package com.vibify.websocket.config;

import com.vibify.websocket.security.JwtHandshakeInterceptor;
import com.vibify.websocket.security.WebSocketAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final WebSocketAuthChannelInterceptor authChannelInterceptor;

    /**
     * Register WebSocket endpoint.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/ws")
                .setAllowedOrigins("*", "null")
                .addInterceptors(jwtHandshakeInterceptor);
    }

    /**
     * Configure STOMP message broker.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        registry.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(webSocketTaskScheduler());

        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Configure inbound channel interceptor for authentication.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

        registration.interceptors(authChannelInterceptor);
    }

    /**
     * Task scheduler used by WebSocket heartbeats.
     */
    @Bean
    public ThreadPoolTaskScheduler webSocketTaskScheduler() {

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("vibify-ws-heartbeat-");
        scheduler.initialize();

        return scheduler;
    }
}