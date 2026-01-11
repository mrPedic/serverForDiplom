package com.example.com.venom.controller.order;

import com.example.com.venom.dto.order.OrderNotificationDto;
import com.example.com.venom.service.order.OrderNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications/order")
@RequiredArgsConstructor
public class OrderNotificationController {

    private final OrderNotificationService notificationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderNotificationDto>> getUserNotifications(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        validateUserAccess(userId, userDetails);

        List<OrderNotificationDto> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        validateUserAccess(userId, userDetails);

        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{notificationId}/read/user/{userId}")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long notificationId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        validateUserAccess(userId, userDetails);

        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all/user/{userId}")
    public ResponseEntity<Void> markAllAsRead(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        validateUserAccess(userId, userDetails);

        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    public void validateUserAccess(Long userId, UserDetails userDetails) {
        // Проверка, что пользователь имеет доступ к этому ресурсу
        // Реализация зависит от вашей системы аутентификации
    }
}