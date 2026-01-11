package com.example.com.venom.service;

import com.example.com.venom.dto.order.OrderDto;
import com.example.com.venom.dto.order.OrderNotificationDto;
import com.example.com.venom.enums.order.OrderNotificationType;
import com.example.com.venom.websocket.CustomNotificationHandler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WebSocketNotificationService {

    private final CustomNotificationHandler notificationHandler;

    public WebSocketNotificationService(CustomNotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
    }

    public void sendOrderNotification(Long userId, OrderNotificationDto notification) {
        // Конвертируем в JSON
        String jsonMessage = convertToJson(notification);
        notificationHandler.sendToUser(userId.toString(), jsonMessage);
    }

    public void sendOrderStatusUpdate(Long orderId, OrderDto order) {
        // Конвертируем заказ в JSON
        String jsonMessage = String.format(
                "{\"type\": \"ORDER_UPDATE\", \"order\": {\"id\": %d, \"status\": \"%s\", \"userId\": %d}}",
                order.getId(),
                order.getStatus().name(),
                order.getUserId()
        );

        notificationHandler.broadcastToChannel("order_" + orderId, jsonMessage);
    }

    public void sendTestNotification(Long userId, String message) {
        String jsonMessage = String.format(
                "{\"type\": \"TEST_NOTIFICATION\", \"message\": \"%s\", \"userId\": \"%d\", \"timestamp\": \"%s\"}",
                message, userId, LocalDateTime.now()
        );

        notificationHandler.sendToUser(userId.toString(), jsonMessage);
    }

    public int broadcastToChannel(String channel, String message) {
        return notificationHandler.broadcastToChannel(channel, message);
    }

    private String convertToJson(OrderNotificationDto notification) {
        return String.format(
                "{\"type\": \"ORDER_NOTIFICATION\", \"data\": {" +
                        "\"id\": %d, " +
                        "\"orderId\": %s, " +
                        "\"userId\": %d, " +
                        "\"type\": \"%s\", " +
                        "\"message\": \"%s\", " +
                        "\"createdAt\": \"%s\"" +
                        "}}",
                notification.getId(),
                notification.getOrderId() != null ? notification.getOrderId().toString() : "null",
                notification.getUserId(),
                notification.getType().name(),
                notification.getMessage(),
                notification.getCreatedAt()
        );
    }
}
