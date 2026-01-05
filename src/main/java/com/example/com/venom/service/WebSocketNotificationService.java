package com.example.com.venom.service;

import com.example.com.venom.websocket.CustomNotificationHandler;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotificationService {

    private final CustomNotificationHandler notificationHandler;

    public WebSocketNotificationService(CustomNotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
    }

    public int broadcastToChannel(String channel, String message) {
        try {
            // Просто вызываем Java-совместимый метод
            return notificationHandler.broadcastToChannelJava(channel, message);
        } catch (Exception e) {
            System.err.println("Error in broadcastToChannel: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}