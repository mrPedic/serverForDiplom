package com.example.com.venom.controller;

import com.example.com.venom.dto.GlobalNotificationDto;
import com.example.com.venom.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class GlobalNotificationController {

    private final WebSocketNotificationService notificationService;

    @PostMapping("/global")
    public ResponseEntity<Void> sendGlobalNotification(@RequestBody GlobalNotificationDto dto) {
        notificationService.sendGlobalNotification(dto);
        return ResponseEntity.ok().build();
    }
}