package com.example.com.venom.dto.order;

import com.example.com.venom.enums.order.OrderNotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderNotificationDto {
    private Long id;
    private Long orderId;
    private Long userId;
    private Long establishmentId;
    private OrderNotificationType type;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
}