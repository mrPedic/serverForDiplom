package com.example.com.venom.entity;

import com.example.com.venom.enums.order.OrderNotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_notifications")
@Getter
@Setter
public class OrderNotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "establishment_id", nullable = false)
    private Long establishmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private OrderNotificationType type;

    @Column(nullable = false)
    private String message;

    @Column(name = "is_read")
    private boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}