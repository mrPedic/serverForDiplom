package com.example.com.venom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.boot.context.properties.bind.Bindable.mapOf;

@RestController
public class TestController {

    @GetMapping("/test/ping")
    public String TestPing(){
        return "pong";
    }

    @GetMapping("/")
    public ResponseEntity<String> Default() throws IOException {
        File htmlFile = new File("C:\\Users\\vladv\\сервер\\venom\\src\\main\\resources\\static\\hello.html");
        String content = Files.readString(htmlFile.toPath());
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(content);
    }


    @GetMapping("/test/")
    public String home() {
        return "Venom server is running! " + LocalDateTime.now();
    }

    @GetMapping("/endpoints")
    public ResponseEntity<Map<String, String>> getEndpoints() {
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("stomp_sockjs", "/ws/notifications (STOMP + SockJS)");
        endpoints.put("plain_websocket", "/ws/plain (Plain WebSocket)");
        endpoints.put("test_rest", "/api/test/send-notification/{userId} (REST API для теста)");
        endpoints.put("ws_stats", "/api/websocket/stats (Статистика WebSocket)");

        return ResponseEntity.ok(endpoints);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("websocket_enabled", true);
        health.put("timestamp", java.time.Instant.now().toString());

        return ResponseEntity.ok(health);
    }
}