package com.example.com.venom.websocket;

import com.example.com.venom.service.SubscriptionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CustomNotificationHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomNotificationHandler.class);

    private final SubscriptionService subscriptionService;
    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private static final long PING_INTERVAL_MS = 30000L;
    private static final int MAX_MESSAGE_SIZE = 65536;
    private static final long SESSION_TIMEOUT_MINUTES = 30L;

    private final Map<String, Object> sessionLocks = new ConcurrentHashMap<>(); // Лок на сессию: sessionId -> lock object

    public CustomNotificationHandler(SubscriptionService subscriptionService, ObjectMapper objectMapper) {
        this.subscriptionService = subscriptionService;
        this.objectMapper = objectMapper;

        // Запускаем периодическую очистку неактивных сессий
        scheduler.scheduleAtFixedRate(() -> {
            try {
                int cleaned = subscriptionService.cleanupInactiveSessions(30);
                if (cleaned > 0) {
                    logger.info("Cleaned up {} inactive sessions", cleaned);
                }
            } catch (Exception e) {
                logger.error("Error during session cleanup: {}", e.getMessage());
            }
        }, 30, 30, TimeUnit.MINUTES);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);

        String userId = (String) session.getAttributes().get("userId");
        if (userId == null) {
            logger.warn("WebSocket connection established without userId, session: {}", session.getId());
            try {
                session.close(CloseStatus.BAD_DATA.withReason("UserId required"));
            } catch (IOException e) {
                logger.error("Error closing session: {}", e.getMessage());
            }
            return;
        }

        String token = (String) session.getAttributes().get("token");
        if (!validateToken(token)) {
            logger.warn("Invalid token for user {}, session: {}", userId, session.getId());
            try {
                session.close(CloseStatus.BAD_DATA.withReason("Invalid token"));
            } catch (IOException e) {
                logger.error("Error closing session: {}", e.getMessage());
            }
            return;
        }

        executorService.submit(() -> {
            try {
                subscriptionService.registerConnection(userId, session.getId());
                logger.info("User {} connected with session {} from {}",
                        userId, session.getId(), session.getRemoteAddress());

                // Отправляем приветственное сообщение
                safeSend(session, String.format(
                        "{\"type\": \"connected\", \"sessionId\": \"%s\", \"userId\": \"%s\", \"timestamp\": \"%d\"}",
                        session.getId(), userId, Instant.now().getEpochSecond()));

                // 🔥 ОТПРАВЛЯЕМ ТЕСТОВОЕ УВЕДОМЛЕНИЕ ПРИ ПОДКЛЮЧЕНИИ
                sendTestNotification(userId, session.getId(), "connection_established");

                // Запускаем пинг
                startPing(session);

            } catch (Exception e) {
                logger.error("Error registering connection for user {}, session {}: {}",
                        userId, session.getId(), e.getMessage(), e);
                safeClose(session, CloseStatus.SERVER_ERROR.withReason("Internal server error"));
            }
        });

        logger.info("🟢 WebSocket connection established: {}", session.getId());
        logger.info("🟢 User ID from attributes: {}", session.getAttributes().get("userId"));
        logger.info("🟢 Remote address: {}", session.getRemoteAddress());
        logger.info("🟢 URI: {}", session.getUri());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        executorService.submit(() -> {
            try {
                // Проверка размера сообщения
                if (message.getPayloadLength() > MAX_MESSAGE_SIZE) {
                    sendError(session, "Message too large", "");
                    return;
                }

                JsonNode request = objectMapper.readTree(message.getPayload());
                String type = request.has("type") ? request.get("type").asText() : "";
                String requestId = request.has("requestId") ? request.get("requestId").asText() : "";

                if (type.isBlank()) {
                    sendError(session, "Message type is required", requestId);
                    return;
                }

                // Обновляем активность сессии
                subscriptionService.updateActivity(session.getId());

                switch (type) {
                    case "subscribe":
                        handleSubscribe(session, request, requestId);
                        // 🔥 ОТПРАВЛЯЕМ ТЕСТОВОЕ УВЕДОМЛЕНИЕ ПРИ ПОДПИСКЕ
                        String channel = request.has("channel") ? request.get("channel").asText() : "";
                        if (!channel.isEmpty()) {
                            sendTestNotificationToChannel(channel, "subscription_confirmed", session.getId());
                        }
                        break;
                    case "unsubscribe":
                        handleUnsubscribe(session, request, requestId);
                        break;
                    case "ping":
                        handlePing(session, requestId);
                        // 🔥 ОТПРАВЛЯЕМ ТЕСТОВОЕ УВЕДОМЛЕНИЕ ПРИ PING
                        String userId = subscriptionService.getUserBySession(session.getId());
                        if (userId != null) {
                            sendTestNotification(userId, session.getId(), "ping_received");
                        }
                        break;
                    case "pong":
                        logger.debug("Received pong from session {}", session.getId());
                        // 🔥 ОТПРАВЛЯЕМ ТЕСТОВОЕ УВЕДОМЛЕНИЕ ПРИ PONG
                        String userForPong = subscriptionService.getUserBySession(session.getId());
                        if (userForPong != null) {
                            sendTestNotification(userForPong, session.getId(), "pong_received");
                        }
                        break;
                    case "auth":
                        handleAuth(session, request, requestId);
                        break;
                    case "test_notification": // 🔥 НОВЫЙ ТИП: запрос тестового уведомления
                        handleTestNotification(session, request, requestId);
                        break;
                    default:
                        sendError(session, "Unknown message type: " + type, requestId);
                }
            } catch (Exception e) {
                logger.error("Error processing message from session {}: {}",
                        session.getId(), e.getMessage(), e);
                sendError(session, "Invalid message format", "");
            }
        });
    }

    private void handleTestNotification(WebSocketSession session, JsonNode request, String requestId) {
        String userId = subscriptionService.getUserBySession(session.getId());
        if (userId == null) {
            sendError(session, "User not found", requestId);
            return;
        }

        // Отправляем подтверждение
        safeSend(session, String.format(
                "{\"type\": \"test_notification_ack\", \"requestId\": \"%s\", \"timestamp\": \"%d\"}",
                requestId, Instant.now().getEpochSecond()));

        // 🔥 ОТПРАВЛЯЕМ ТЕСТОВОЕ УВЕДОМЛЕНИЕ
        String notificationType = request.has("notificationType") ?
                request.get("notificationType").asText() : "manual_test";
        int count = sendTestNotification(userId, session.getId(), notificationType);

        logger.info("Test notification sent to user {} ({} sessions), type: {}",
                userId, count, notificationType);
    }

    // 🔥 МЕТОД ДЛЯ ОТПРАВКИ ТЕСТОВОГО УВЕДОМЛЕНИЯ ПОЛЬЗОВАТЕЛЮ
    private int sendTestNotification(String userId, String sessionId, String trigger) {
        try {
            String testMessage = String.format(
                    "{\"type\": \"TEST_NOTIFICATION\", \"data\": {" +
                            "\"message\": \"Тестовое уведомление\", " +
                            "\"userId\": \"%s\", " +
                            "\"sessionId\": \"%s\", " +
                            "\"trigger\": \"%s\", " +
                            "\"timestamp\": \"%d\", " +
                            "\"serverTime\": \"%s\", " +
                            "\"testId\": \"test_%d\"}}",
                    userId, sessionId, trigger,
                    Instant.now().getEpochSecond(),
                    Instant.now().toString(),
                    System.currentTimeMillis());

            // Отправляем на канал пользователя
            int channelSent = broadcastToChannel("user_" + userId, testMessage);

            // Отправляем напрямую всем сессиям пользователя
            int directSent = sendToUser(userId, testMessage);

            logger.info("📤 Test notification sent - Channel: {}, Direct: {}, User: {}, Trigger: {}",
                    channelSent, directSent, userId, trigger);

            return Math.max(channelSent, directSent);
        } catch (Exception e) {
            logger.error("Error sending test notification: {}", e.getMessage());
            return 0;
        }
    }

    // 🔥 МЕТОД ДЛЯ ОТПРАВКИ ТЕСТОВОГО УВЕДОМЛЕНИЯ В КАНАЛ
    private int sendTestNotificationToChannel(String channel, String trigger, String sessionId) {
        try {
            String testMessage = String.format(
                    "{\"type\": \"TEST_CHANNEL_NOTIFICATION\", \"data\": {" +
                            "\"message\": \"Тестовое уведомление в канал\", " +
                            "\"channel\": \"%s\", " +
                            "\"trigger\": \"%s\", " +
                            "\"sessionId\": \"%s\", " +
                            "\"timestamp\": \"%d\", " +
                            "\"testId\": \"channel_test_%d\"}}",
                    channel, trigger, sessionId,
                    Instant.now().getEpochSecond(),
                    System.currentTimeMillis());

            return broadcastToChannel(channel, testMessage);
        } catch (Exception e) {
            logger.error("Error sending channel test notification: {}", e.getMessage());
            return 0;
        }
    }

    // ... остальные методы остаются без изменений ...
    private void handleSubscribe(WebSocketSession session, JsonNode request, String requestId) {
        String channel = request.has("channel") ? request.get("channel").asText() : "";
        if (channel.isEmpty()) {
            sendError(session, "Channel is required", requestId);
            return;
        }

        boolean success = subscriptionService.subscribe(session.getId(), channel);
        if (success) {
            safeSend(session, String.format(
                    "{\"type\": \"subscribed\", \"channel\": \"%s\", \"requestId\": \"%s\", \"timestamp\": \"%d\"}",
                    channel, requestId, Instant.now().getEpochSecond()));
            logger.info("Session {} subscribed to channel {}", session.getId(), channel);
        } else {
            sendError(session, "Subscription failed: session not registered", requestId);
        }
    }

    private void handleUnsubscribe(WebSocketSession session, JsonNode request, String requestId) {
        String channel = request.has("channel") ? request.get("channel").asText() : "";
        if (channel.isEmpty()) {
            sendError(session, "Channel is required", requestId);
            return;
        }

        boolean unsubscribed = subscriptionService.unsubscribe(session.getId(), channel);
        if (unsubscribed) {
            safeSend(session, String.format(
                    "{\"type\": \"unsubscribed\", \"channel\": \"%s\", \"requestId\": \"%s\"}",
                    channel, requestId));
        }
    }

    private void handlePing(WebSocketSession session, String requestId) {
        safeSend(session, String.format(
                "{\"type\": \"pong\", \"timestamp\": \"%d\", \"requestId\": \"%s\"}",
                Instant.now().getEpochSecond(), requestId));
    }

    private void handleAuth(WebSocketSession session, JsonNode request, String requestId) {
        String newToken = request.has("token") ? request.get("token").asText() : "";
        if (!validateToken(newToken)) {
            sendError(session, "Invalid token", requestId);
            return;
        }
        safeSend(session, String.format(
                "{\"type\": \"auth_success\", \"requestId\": \"%s\"}", requestId));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // Проверяем, не была ли сессия уже удалена при обработке EOF
        if (sessions.containsKey(session.getId())) {
            sessions.remove(session.getId());

            executorService.submit(() -> {
                String userId = subscriptionService.getUserBySession(session.getId());
                subscriptionService.unsubscribeAll(session.getId());
                logger.info("User {} disconnected with status: {} (code: {}, reason: {})",
                        userId, status, status.getCode(), status.getReason());
            });
        }
    }

    private boolean safeSend(WebSocketSession session, String message) {
        if (!session.isOpen()) {
            logger.warn("Cannot send: session {} is closed", session.getId());
            return false;
        }

        // Получаем или создаём лок для этой сессии
        Object lock = sessionLocks.computeIfAbsent(session.getId(), k -> new Object());

        synchronized (lock) {  // Синхронизируем отправку
            try {
                session.sendMessage(new TextMessage(message));
                return true;
            } catch (IllegalStateException ise) {
                logger.error("Illegal state during send to {}: {}", session.getId(), ise.getMessage());
                return false;
            } catch (IOException ioe) {
                logger.error("IO error sending to {}: {}", session.getId(), ioe.getMessage());
                safeClose(session, CloseStatus.PROTOCOL_ERROR);
                return false;
            } catch (Exception e) {
                logger.error("Unexpected error sending to {}: {}", session.getId(), e.getMessage(), e);
                return false;
            }
        }
    }

    private void sendError(WebSocketSession session, String message, String requestId) {
        executorService.submit(() -> safeSend(session, String.format(
                "{\"type\": \"error\", \"message\": \"%s\", \"requestId\": \"%s\", \"timestamp\": \"%d\"}",
                message, requestId, Instant.now().getEpochSecond())));
    }

    public int sendToUser(String userId, String message) {
        AtomicInteger sentCount = new AtomicInteger(0);
        Set<String> connections = subscriptionService.getConnections(userId);

        connections.forEach(sessionId -> {
            WebSocketSession session = sessions.get(sessionId);
            if (session != null && safeSend(session, message)) {
                sentCount.incrementAndGet();
            }
        });

        if (sentCount.get() == 0 && !connections.isEmpty()) {
            logger.warn("Failed to send message to user {}, all sessions appear dead", userId);
            // Очищаем мертвые сессии
            connections.forEach(sessionId -> {
                if (sessions.get(sessionId) == null || !sessions.get(sessionId).isOpen()) {
                    subscriptionService.unsubscribeAll(sessionId);
                }
            });
        }

        return sentCount.get();
    }

    public int broadcastToChannel(String channel, String message) {
        AtomicInteger sentCount = new AtomicInteger(0);
        Set<String> subscribers = subscriptionService.getSubscribers(channel);

        subscribers.forEach(sessionId -> {
            WebSocketSession session = sessions.get(sessionId);
            if (session != null && safeSend(session, message)) {
                sentCount.incrementAndGet();
            }
        });

        logger.info("Broadcasted to {}/{} subscribers on channel {}",
                sentCount.get(), subscribers.size(), channel);

        return sentCount.get();
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        if (exception instanceof IOException) {
            String message = exception.getMessage();
            if (message != null && message.contains("EOF")) {
                logger.info("WebSocket connection closed by client for session {}", session.getId());
                // Не пытаемся закрыть сессию - она уже закрыта клиентом
                // Просто выполняем очистку
                executorService.submit(() -> {
                    String userId = subscriptionService.getUserBySession(session.getId());
                    subscriptionService.unsubscribeAll(session.getId());
                    sessions.remove(session.getId());
                    logger.info("User {} session {} cleaned up after client disconnect",
                            userId, session.getId());
                });
                return;
            }
        }

        logger.error("Transport error for session {}: {}", session.getId(), exception.getMessage(), exception);
        executorService.submit(() -> safeClose(session, CloseStatus.SERVER_ERROR.withReason("Transport error")));
    }

    private void startPing(WebSocketSession session) {
        scheduler.scheduleAtFixedRate(() -> {
            if (session.isOpen()) {
                try {
                    if (!safeSend(session, String.format(
                            "{\"type\": \"ping\", \"timestamp\": \"%d\"}",
                            Instant.now().getEpochSecond()))) {
                        // Если отправка не удалась, останавливаем пинг для этой сессии
                        throw new RuntimeException("Ping failed");
                    }
                } catch (Exception e) {
                    logger.error("Ping failed for session {}", session.getId(), e);
                    // Останавливаем задачу при ошибке
                    throw new RuntimeException(e);
                }
            }
        }, PING_INTERVAL_MS, PING_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private void safeClose(WebSocketSession session, CloseStatus status) {
        try {
            if (session != null && session.isOpen()) {
                session.close(status);
            }
        } catch (IllegalStateException e) {
            // Сессия уже закрыта - это нормально
            logger.debug("Session {} already closed", session.getId());
        } catch (Exception e) {
            logger.error("Error closing session {}: {}", session.getId(), e.getMessage());
        }
    }

    private boolean validateToken(String token) {
        // В реальном приложении здесь должна быть проверка JWT или другого токена
        return token != null && !token.isEmpty() && !token.equals("invalid");
    }

    @PreDestroy
    public void destroy() {
        logger.info("Shutting down WebSocket handler...");

        // Закрываем все активные сессии
        sessions.values().forEach(session -> {
            safeClose(session, CloseStatus.GOING_AWAY.withReason("Server shutdown"));
        });

        // Очищаем все коллекции
        sessions.clear();
        sessionLocks.clear();

        // Останавливаем executor'ы
        scheduler.shutdown();
        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("WebSocket handler shutdown completed");
    }

    // Java-совместимые методы (те же самые, но для совместимости)
    public int broadcastToChannelJava(String channel, String message) {
        return broadcastToChannel(channel, message);
    }

    public int sendToUserJava(String userId, String message) {
        return sendToUser(userId, message);
    }
}