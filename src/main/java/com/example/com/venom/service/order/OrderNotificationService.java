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
    private final UserRepository userRepository; // Добавлено для получения имени пользователя
    private final EstablishmentRepository establishmentRepository;


    @Transactional
    public void sendOrderCreatedNotification(OrderEntity order) {
        // Достаем имя пользователя для красивого сообщения
        String userName = userRepository.findById(order.getUserId())
                .map(UserEntity::getName)
                .orElse("Клиент #" + order.getUserId());

        OrderNotificationEntity notification = new OrderNotificationEntity();
        notification.setOrderId(order.getId()); // Работаем с Long ID
        notification.setUserId(order.getUserId());
        notification.setEstablishmentId(order.getEstablishmentId());
        notification.setType(OrderNotificationType.ORDER_CREATED);
        notification.setMessage(String.format(
                "Создан новый заказ #%d от пользователя %s",
                order.getId(), userName
        ));

        notificationRepository.save(notification);

        // Отправляем через WebSocket уведомление
        webSocketService.sendOrderNotification(order.getUserId(), convertToDto(notification));
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

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        OrderNotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Уведомление не найдено"));

        // Исправлено: сравниваем Long ID напрямую
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

    public OrderNotificationDto convertToDto(OrderNotificationEntity entity) {
        return OrderNotificationDto.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId()) // Исправлено на прямое получение ID
                .userId(entity.getUserId())   // Исправлено на прямое получение ID
                .establishmentId(entity.getEstablishmentId())
                .type(entity.getType())
                .message(entity.getMessage())
                .isRead(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // OrderNotificationService.java - ДОБАВЬ в конец класса

    @Transactional
    public void sendOrderStatusChangedNotification(OrderEntity order) {
        // Достаем имя пользователя
        String userName = userRepository.findById(order.getUserId())
                .map(UserEntity::getName)
                .orElse("Клиент #" + order.getUserId());

        OrderNotificationEntity notification = new OrderNotificationEntity();
        notification.setOrderId(order.getId());
        notification.setUserId(order.getUserId());
        notification.setEstablishmentId(order.getEstablishmentId());
        notification.setType(OrderNotificationType.ORDER_STATUS_CHANGED);
        notification.setMessage(String.format(
                "Статус заказа #%d изменен на: %s",
                order.getId(), order.getStatus().toString()
        ));

        notificationRepository.save(notification);

        // Отправляем через WebSocket уведомление пользователю
        webSocketService.sendOrderNotification(order.getUserId(), convertToDto(notification));

        // Также отправляем уведомление владельцу заведения
        sendEstablishmentOwnerNotification(order, userName);
    }

    private void sendEstablishmentOwnerNotification(OrderEntity order, String userName) {
        try {
            // Получаем владельца заведения
            EstablishmentEntity establishment = establishmentRepository.findById(order.getEstablishmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Заведение не найдено"));

            Long ownerId = establishment.getCreatedUserId();

            if (ownerId != null) {
                // Создаем уведомление для владельца
                OrderNotificationEntity ownerNotification = new OrderNotificationEntity();
                ownerNotification.setOrderId(order.getId());
                ownerNotification.setUserId(ownerId); // Владелец заведения
                ownerNotification.setEstablishmentId(order.getEstablishmentId());
                ownerNotification.setType(OrderNotificationType.ORDER_STATUS_CHANGED);
                ownerNotification.setMessage(String.format(
                        "Заказ #%d от %s: статус изменен на %s",
                        order.getId(), userName, order.getStatus().toString()
                ));

                notificationRepository.save(ownerNotification);

                // Отправляем через WebSocket владельцу
                webSocketService.sendOrderNotification(ownerId, convertToDto(ownerNotification));
            }
        } catch (Exception e) {
            System.err.println("Ошибка отправки уведомления владельцу: " + e.getMessage());
        }
    }

    // Метод для отправки уведомления об отклонении заказа
    @Transactional
    public void sendOrderRejectedNotification(OrderEntity order, String rejectionReason) {
        String userName = userRepository.findById(order.getUserId())
                .map(UserEntity::getName)
                .orElse("Клиент #" + order.getUserId());

        OrderNotificationEntity notification = new OrderNotificationEntity();
        notification.setOrderId(order.getId());
        notification.setUserId(order.getUserId());
        notification.setEstablishmentId(order.getEstablishmentId());
        notification.setType(OrderNotificationType.ORDER_REJECTED);
        notification.setMessage(String.format(
                "Заказ #%d отклонен. Причина: %s",
                order.getId(), rejectionReason != null ? rejectionReason : "Не указана"
        ));

        notificationRepository.save(notification);
        webSocketService.sendOrderNotification(order.getUserId(), convertToDto(notification));
    }
}