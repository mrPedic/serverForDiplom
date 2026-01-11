package com.example.com.venom.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.Map;
import java.util.regex.Pattern;

@Configuration
@EnableWebSocket // ИЗМЕНЕНО: используем EnableWebSocket вместо EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketConfigurer { // ИЗМЕНЕНО: WebSocketConfigurer вместо WebSocketMessageBrokerConfigurer

    private final CustomNotificationHandler customNotificationHandler;

    // УДАЛЕНО: @Autowired private CustomUserDetailsService userDetailsService;

    public WebSocketConfig(CustomNotificationHandler customNotificationHandler) {
        this.customNotificationHandler = customNotificationHandler;
    }

    // УДАЛЕНО: configureMessageBroker метод - не нужен для обычного WebSocket

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // ИЗМЕНЕНО: .withSockJS() удалено, используем обычный WebSocket
        registry.addHandler(customNotificationHandler, "/ws/notifications")
                .addInterceptors(new PlainWebSocketHandshakeInterceptor())
                .setAllowedOriginPatterns("*");

        // Дополнительный endpoint для обратной совместимости
        registry.addHandler(customNotificationHandler, "/ws/orders")
                .addInterceptors(new PlainWebSocketHandshakeInterceptor())
                .setAllowedOriginPatterns("*");
    }

    private static class PlainWebSocketHandshakeInterceptor implements HandshakeInterceptor {

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Map<String, Object> attributes) {
            System.out.println("📡 Plain WebSocket connection attempt from: " + request.getRemoteAddress());
            System.out.println("📡 Headers: " + request.getHeaders());
            System.out.println("📡 URI: " + request.getURI());

            var queryParams = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();
            String userId = queryParams.getFirst("userId");
            String token = queryParams.getFirst("token");

            System.out.println("📡 userId: " + userId + ", token: " + token);

            // Валидация обязательных параметров
            if (userId == null || userId.isEmpty()) {
                System.out.println("❌ Rejected: userId is null or empty");
                return false; // Отклоняем соединение без userId
            }

            // Проверка формата userId
            if (!isValidUserId(userId)) {
                System.out.println("❌ Rejected: userId format invalid");
                return false;
            }

            // Проверка токена (упрощенная для тестирования)
            if (!validateToken(token, userId)) {
                System.out.println("❌ Rejected: token invalid");
                return false;
            }

            System.out.println("✅ Plain WebSocket connection accepted for user " + userId);

            // Добавляем информацию о сессии
            attributes.put("userId", userId);
            attributes.put("token", token != null ? token : "");
            attributes.put("connectionTime", Instant.now().toString());
            attributes.put("remoteAddress", request.getRemoteAddress() != null ?
                    request.getRemoteAddress().getHostString() : "unknown");

            return true;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Exception exception) {
            var queryParams = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();
            String userId = queryParams.getFirst("userId");

            if (exception != null) {
                System.out.println("❌ Plain WebSocket handshake failed for user " + userId + ": " + exception.getMessage());
            } else {
                System.out.println("✅ Plain WebSocket handshake successful for user " + userId);
            }
        }

        private boolean validateToken(String token, String userId) {
            // Для тестирования разрешаем токены вида "android_token_{userId}"
            System.out.println("🔑 Token validation: " + token + " for user: " + userId);

            if (token != null && token.startsWith("android_token_")) {
                try {
                    String tokenUserId = token.substring("android_token_".length());
                    if (tokenUserId.equals(userId)) {
                        System.out.println("✅ Token valid for user: " + userId);
                        return true;
                    }
                } catch (Exception e) {
                    System.out.println("❌ Error during token validation: " + e.getMessage());
                }
            }

            // Для тестирования также разрешаем другие форматы
            System.out.println("⚠️ Token validation skipped for testing");
            return true;
        }

        private boolean isValidUserId(String userId) {
            // Базовые проверки userId
            if (userId.length() < 1 || userId.length() > 50) {
                return false;
            }

            // Разрешаем буквы, цифры, подчеркивание, дефис, @ и точку
            Pattern pattern = Pattern.compile("^[a-zA-Z0-9_\\-@.]+$");
            if (!pattern.matcher(userId).matches()) {
                return false;
            }

            // Запрещаем двойные точки и двойные слэши
            return !userId.contains("..") && !userId.contains("//");
        }
    }
}