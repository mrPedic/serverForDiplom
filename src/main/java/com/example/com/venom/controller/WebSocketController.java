package com.example.com.venom.controller;

import com.example.com.venom.service.SubscriptionService;
import com.example.com.venom.websocket.CustomNotificationHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/websocket")
public class WebSocketController {

    private final CustomNotificationHandler notificationHandler;
    private final SubscriptionService subscriptionService;

    public WebSocketController(CustomNotificationHandler notificationHandler,
                               SubscriptionService subscriptionService) {
        this.notificationHandler = notificationHandler;
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/send-to-user/{userId}")
    public ResponseEntity<Map<String, Object>> sendToUser(
            @PathVariable String userId,
            @RequestBody String message) {

        if (message.length() > 65536) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Message too large");
            error.put("maxSize", 65536);
            error.put("actualSize", message.length());
            return ResponseEntity.badRequest().body(error);
        }

        int sentCount = notificationHandler.sendToUserJava(userId, message);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "message_sent");
        response.put("userId", userId);
        response.put("sentToConnections", sentCount);
        response.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/broadcast/{channel}")
    public ResponseEntity<Map<String, Object>> broadcastToChannel(
            @PathVariable String channel,
            @RequestBody String message) {

        if (message.length() > 65536) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Message too large");
            error.put("maxSize", 65536);
            error.put("actualSize", message.length());
            return ResponseEntity.badRequest().body(error);
        }

        int sentCount = notificationHandler.broadcastToChannelJava(channel, message);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "broadcast_sent");
        response.put("channel", channel);
        response.put("sentToSubscribers", sentCount);
        response.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = subscriptionService.getStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> stats = subscriptionService.getStats();
        int totalConnections = (int) stats.getOrDefault("totalConnections", 0);
        boolean healthy = totalConnections >= 0;

        Map<String, Object> response = new HashMap<>();
        response.put("status", healthy ? "UP" : "DOWN");
        response.put("timestamp", Instant.now().toString());
        response.put("details", stats);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/connections")
    public ResponseEntity<Map<String, Object>> getUserConnections(@PathVariable String userId) {
        var connections = subscriptionService.getConnections(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("connections", connections);
        response.put("count", connections.size());
        response.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/cleanup-inactive")
    public ResponseEntity<Map<String, Object>> cleanupInactiveSessions(
            @RequestParam(defaultValue = "30") long timeoutMinutes) {

        int cleanedCount = subscriptionService.cleanupInactiveSessions(timeoutMinutes);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "cleanup_completed");
        response.put("cleanedSessions", cleanedCount);
        response.put("timeoutMinutes", timeoutMinutes);
        response.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active-users")
    public ResponseEntity<Map<String, Object>> getActiveUsers() {
        var activeUsers = subscriptionService.getActiveUsers();

        Map<String, Object> response = new HashMap<>();
        response.put("activeUsers", activeUsers);
        response.put("count", activeUsers.size());
        response.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/ws-url")
    public ResponseEntity<Map<String, String>> getWsUrl() {
        Map<String, String> response = new HashMap<>();
        response.put("wsUrl", "ws://localhost:8080/ws/notifications");
        response.put("note", "Add ?userId=YOUR_ID&token=YOUR_TOKEN");

        return ResponseEntity.ok(response);
    }
}