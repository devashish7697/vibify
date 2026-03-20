package com.vibify.websocket.security;

import com.vibify.auth.security.JwtService;
import com.vibify.user.model.User;
import com.vibify.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    /**
     * Executed before WebSocket handshake is established.
     * Responsible for authenticating the user using JWT.
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        try {

            if (!(request instanceof ServletServerHttpRequest servletRequest)) {
                log.warn("Handshake request is not a ServletServerHttpRequest");
                return false;
            }

            HttpServletRequest httpRequest = servletRequest.getServletRequest();

            // 1. Try Authorization header (for future mobile apps)
            String authHeader = httpRequest.getHeader("Authorization");

            String token = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            } else {
                // 2. Fallback to query param (for browser testing)
                String query = httpRequest.getQueryString();

                if (query != null && query.contains("token=")) {
                    token = query.split("token=")[1];
                }
            }

            if (token == null) {
                log.warn("WebSocket connection rejected: No token provided");
                return false;
            }

            String username = jwtService.extractUsername(token);

            if (username == null) {
                log.warn("WebSocket connection rejected: Token does not contain username");
                return false;
            }

            Optional<User> userOptional = userRepository.findByUsername(username);

            if (userOptional.isEmpty()) {
                log.warn("WebSocket connection rejected: User not found for username={}", username);
                return false;
            }

            User user = userOptional.get();

            boolean tokenValid = jwtService.isTokenValid(token, username);

            if (!tokenValid) {
                log.warn("WebSocket connection rejected: Invalid JWT token for user={}", username);
                return false;
            }

            /*
             * Store authenticated identity in WebSocket session attributes.
             * This will be available in:
             *
             * - ChannelInterceptor
             * - WebSocket controllers
             */
            attributes.put("userId", user.getId());
            attributes.put("username", user.getUsername());

            log.debug("WebSocket handshake successful for userId={}", user.getId());

            return true;

        } catch (Exception ex) {

            log.error("WebSocket handshake authentication failed", ex);
            return false;
        }
    }

    /**
     * Executed after handshake is completed.
     * Not required for authentication logic.
     */
    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {

        if (exception != null) {
            log.error("WebSocket handshake completed with exception", exception);
        }
    }
}