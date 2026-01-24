package com.example.com.venom.service.order;

import com.example.com.venom.dto.order.OrderNotificationDto;
import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.entity.OrderEntity;
import com.example.com.venom.entity.OrderNotificationEntity;
import com.example.com.venom.entity.UserEntity;
import com.example.com.venom.enums.order.OrderNotificationType;
import com.example.com.venom.repository.EstablishmentRepository;
import com.example.com.venom.repository.UserRepository;
import com.example.com.venom.repository.order.OrderNotificationRepository;
import com.example.com.venom.service.WebSocketNotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderNotificationService {

    private final OrderNotificationRepository notificationRepository;
    private final WebSocketNotificationService webSocketService;
    private final UserRepository userRepository;
    private final EstablishmentRepository establishmentRepository;

    /**
     * Сценарий 1: Пользователь создал заказ -> Уведомляем АДМИНИСТРАТОРА (Владельца) заведения.
     */
    @Transactional
    public void sendOrderCreatedNotification(OrderEntity order) {
        // 1. Находим заведение и его владельца
        EstablishmentEntity establishment = establishmentRepository.findById(order.getEstablishmentId())
                .orElseThrow(() -> new EntityNotFoundException("Заведение не найдено"));

        Long ownerId = establishment.getCreatedUserId(); // ID владельца заведения

        // 2. Получаем имя клиента для сообщения
        String clientName = userRepository.findById(order.getUserId())
                .map(UserEntity::getName)
                .orElse("Клиент #" + order.getUserId());

        // 3. Формируем сообщение
        String message = String.format("Новый заказ #%d от %s на сумму %.2f",
                order.getId(), clientName, order.getTotalPrice());

        // 4. Если у заведения есть владелец, сохраняем уведомление в БД для него и отправляем в WebSocket
        if (ownerId != null) {
            OrderNotificationEntity adminNotification = new OrderNotificationEntity();
            adminNotification.setOrderId(order.getId());
            adminNotification.setUserId(ownerId); // <-- Уведомление для ВЛАДЕЛЬЦА
            adminNotification.setEstablishmentId(order.getEstablishmentId());
            adminNotification.setType(OrderNotificationType.ORDER_CREATED);
            adminNotification.setMessage(message);

            notificationRepository.save(adminNotification);

            // Отправляем лично владельцу
            webSocketService.sendOrderNotification(ownerId, convertToDto(adminNotification));
        }

        // 5. ДОПОЛНИТЕЛЬНО: Отправляем уведомление в общий канал заведения (для планшетов поваров/кассиров)
        // Это не требует сохранения в БД для конкретного юзера, это бродкаст
        webSocketService.broadcastToEstablishment(
                establishment.getId(),
                convertToDto(createTempNotificationForBroadcast(order, message))
        );
    }

    /**
     * Сценарий 2: Статус заказа изменился -> Уведомляем ПОЛЬЗОВАТЕЛЯ (Клиента).
     */
    @Transactional
    public void sendOrderStatusChangedNotification(OrderEntity order) {
        // 1. Формируем сообщение для клиента
        String message = String.format("Статус вашего заказа #%d изменен на: %s",
                order.getId(), order.getStatus());

        // 2. Создаем запись в БД для КЛИЕНТА
        OrderNotificationEntity userNotification = new OrderNotificationEntity();
        userNotification.setOrderId(order.getId());
        userNotification.setUserId(order.getUserId()); // <-- Уведомление для КЛИЕНТА
        userNotification.setEstablishmentId(order.getEstablishmentId());
        userNotification.setType(OrderNotificationType.ORDER_STATUS_CHANGED);
        userNotification.setMessage(message);

        notificationRepository.save(userNotification);

        // 3. Отправляем WebSocket сообщение клиенту
        webSocketService.sendOrderNotification(order.getUserId(), convertToDto(userNotification));
    }

    /**
     * Сценарий 3: Заказ отклонен -> Уведомляем ПОЛЬЗОВАТЕЛЯ.
     */
    @Transactional
    public void sendOrderRejectedNotification(OrderEntity order, String rejectionReason) {
        String message = String.format("Заказ #%d отклонен. Причина: %s",
                order.getId(), rejectionReason != null ? rejectionReason : "Не указана");

        OrderNotificationEntity notification = new OrderNotificationEntity();
        notification.setOrderId(order.getId());
        notification.setUserId(order.getUserId()); // <-- Для КЛИЕНТА
        notification.setEstablishmentId(order.getEstablishmentId());
        notification.setType(OrderNotificationType.ORDER_REJECTED);
        notification.setMessage(message);

        notificationRepository.save(notification);
        webSocketService.sendOrderNotification(order.getUserId(), convertToDto(notification));
    }

    // --- Вспомогательные методы ---

    public List<OrderNotificationDto> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        OrderNotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Уведомление не найдено"));

        if (!notification.getUserId().equals(userId)) {
            throw new AccessDeniedException("Уведомление принадлежит другому пользователю");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<OrderNotificationEntity> unread = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    private OrderNotificationDto convertToDto(OrderNotificationEntity entity) {
        return OrderNotificationDto.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .userId(entity.getUserId())
                .establishmentId(entity.getEstablishmentId())
                .type(entity.getType())
                .message(entity.getMessage())
                .isRead(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // Создает временный объект сущности только для конвертации в DTO при бродкасте
    private OrderNotificationEntity createTempNotificationForBroadcast(OrderEntity order, String message) {
        OrderNotificationEntity temp = new OrderNotificationEntity();
        temp.setId(0L); // ID не важен для сокета
        temp.setOrderId(order.getId());
        temp.setUserId(order.getUserId());
        temp.setEstablishmentId(order.getEstablishmentId());
        temp.setType(OrderNotificationType.ORDER_CREATED);
        temp.setMessage(message);
        temp.setRead(false);
        // createdAt проставится автоматически или будет null, но для сокета это не критично сейчас
        return temp;
    }
}