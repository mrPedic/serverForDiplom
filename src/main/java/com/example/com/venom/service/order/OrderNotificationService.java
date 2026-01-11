package com.example.com.venom.service.order;

import com.example.com.venom.dto.order.OrderNotificationDto;
import com.example.com.venom.entity.OrderEntity;
import com.example.com.venom.entity.OrderNotificationEntity;
import com.example.com.venom.enums.order.OrderNotificationType;
import com.example.com.venom.enums.order.OrderStatus;
import com.example.com.venom.repository.order.OrderNotificationRepository;
import com.example.com.venom.service.WebSocketNotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderNotificationService {

    private final OrderNotificationRepository notificationRepository;
    private final WebSocketNotificationService webSocketService;

    public void sendOrderCreatedNotification(OrderEntity order) {
        OrderNotificationEntity notification = new OrderNotificationEntity();
        notification.setOrder(order);
        notification.setUser(order.getUser());
        notification.setEstablishmentId(order.getEstablishment().getId());
        notification.setType(OrderNotificationType.ORDER_CREATED);
        notification.setMessage(String.format(
                "Создан новый заказ #%d от пользователя %s",
                order.getId(),
                order.getUser().getName()
        ));

        notificationRepository.save(notification);

        // Отправляем через WebSocket пользователю
        webSocketService.sendOrderNotification(
                order.getUser().getId(),
                convertToDto(notification)
        );

        // Также отправляем владельцу заведения (если нужно)
        if (order.getEstablishment().getCreatedUserId() != null) {
            webSocketService.sendOrderNotification(
                    order.getEstablishment().getCreatedUserId(),
                    convertToDto(notification)
            );
        }
    }

    public void sendOrderStatusChangedNotification(OrderEntity order, OrderStatus oldStatus) {
        OrderNotificationEntity notification = new OrderNotificationEntity();
        notification.setOrder(order);
        notification.setUser(order.getUser());
        notification.setEstablishmentId(order.getEstablishment().getId());
        notification.setType(OrderNotificationType.ORDER_STATUS_CHANGED);

        String statusMessage = switch (order.getStatus()) {
            case CONFIRMED -> "подтвержден";
            case IN_PROGRESS -> "готовится";
            case OUT_FOR_DELIVERY -> "в доставке";
            case DELIVERED -> "доставлен";
            case REJECTED -> "отклонен. Причина: " + order.getRejectionReason();
            default -> "изменен";
        };

        notification.setMessage(String.format(
                "Статус заказа #%d изменен: %s",
                order.getId(),
                statusMessage
        ));

        notificationRepository.save(notification);

        // Отправляем через WebSocket пользователю
        webSocketService.sendOrderNotification(
                order.getUser().getId(),
                convertToDto(notification)
        );
    }

    public List<OrderNotificationDto> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void markAsRead(Long notificationId, Long userId) {
        OrderNotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Уведомление не найдено"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Уведомление принадлежит другому пользователю");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(Long userId) {
        List<OrderNotificationEntity> unread = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    private OrderNotificationDto convertToDto(OrderNotificationEntity entity) {
        return OrderNotificationDto.builder()
                .id(entity.getId())
                .orderId(entity.getOrder() != null ? entity.getOrder().getId() : null)
                .userId(entity.getUser().getId())
                .establishmentId(entity.getEstablishmentId())
                .type(entity.getType())
                .message(entity.getMessage())
                .isRead(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
