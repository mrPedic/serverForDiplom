package com.example.com.venom.service;

import com.example.com.venom.dto.order.OrderDto;
import com.example.com.venom.dto.order.OrderNotificationDto;
import com.example.com.venom.websocket.CustomNotificationHandler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WebSocketNotificationService {

    private final CustomNotificationHandler notificationHandler;

    public WebSocketNotificationService(CustomNotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
    }

    /**
     * Общий метод для отправки сообщения в конкретный канал.
     * Именно его не хватало для BookingController.
     */
    public int broadcastToChannel(String channel, String message) {
        return notificationHandler.broadcastToChannel(channel, message);
    }

    // Внутри WebSocketNotificationService.java

    public void sendGlobalNotification(com.example.com.venom.dto.GlobalNotificationDto dto) {
        String jsonMessage = String.format(
                "{\"type\": \"GLOBAL_NOTIFICATION\", \"title\": \"%s\", \"message\": \"%s\", \"timestamp\": \"%s\"}",
                dto.getTitle(),
                dto.getMessage(),
                java.time.LocalDateTime.now()
        );

        String target = dto.getTarget();
        System.out.println("🔔 SERVER RECEIVED NOTIFICATION REQUEST");
        System.out.println("TARGET: " + target);

        if (target == null) return;

        if ("all_users".equals(target)) {
            System.out.println("📤 Broadcasting to 'global' channel...");
            notificationHandler.broadcastToChannel("global", jsonMessage);
        }
        else if (target.startsWith("specific_user:")) {
            String userId = target.split(":")[1];
            notificationHandler.sendToUser(userId, jsonMessage);
        }
        // --- ДОБАВЛЕНО ---
        else if ("all_establishments".equals(target)) {
            System.out.println("📤 Broadcasting to all establishments...");
            notificationHandler.broadcastToChannel("all_establishments", jsonMessage);
        }
        else if (target.startsWith("specific_establishment:")) {
            String estId = target.split(":")[1];
            System.out.println("📤 Sending to establishment channel: establishment_" + estId);
            notificationHandler.broadcastToChannel("establishment_" + estId, jsonMessage);
        }
        else {
            System.err.println("⚠️ Unknown target format: " + target);
        }
    }

    /**
     * Отправка личного уведомления пользователю по его UserID.
     */
    public void sendOrderNotification(Long userId, OrderNotificationDto notification) {
        try {
            String message = String.format(
                    "{\"type\": \"ORDER_NOTIFICATION\", \"data\": %s}",
                    convertToJson(notification)
            );
            notificationHandler.sendToUser(userId.toString(), message);
        } catch (Exception e) {
            System.err.println("Ошибка отправки WebSocket уведомления пользователю: " + e.getMessage());
        }
    }

    /**
     * Рассылка уведомления всем подписчикам канала заведения.
     */
    public void broadcastToEstablishment(Long establishmentId, OrderNotificationDto notification) {
        try {
            String message = String.format(
                    "{\"type\": \"ORDER_NOTIFICATION\", \"data\": %s}",
                    convertToJson(notification)
            );
            String establishmentChannel = "establishment_" + establishmentId;
            notificationHandler.broadcastToChannel(establishmentChannel, message);
        } catch (Exception e) {
            System.err.println("Ошибка бродкаста уведомления заведению: " + e.getMessage());
        }
    }

    /**
     * Уведомление об изменении статуса (старый формат, если используется).
     */
    public void sendOrderStatusUpdate(Long orderId, OrderDto order) {
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

    private String convertToJson(OrderNotificationDto notification) {
        return String.format(
                "{\"id\": %d, \"orderId\": %s, \"userId\": %d, \"type\": \"%s\", \"message\": \"%s\", \"createdAt\": \"%s\"}",
                notification.getId(),
                notification.getOrderId() != null ? notification.getOrderId().toString() : "null",
                notification.getUserId(),
                notification.getType().name(),
                notification.getMessage(),
                notification.getCreatedAt()
        );
    }
}