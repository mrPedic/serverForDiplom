package com.example.com.venom.entity;

import com.example.com.venom.enums.order.OrderStatus;
import com.example.com.venom.enums.order.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Просто ID без связей
    @Column(name = "establishment_id", nullable = false)
    private Long establishmentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "delivery_address_id")
    private Long deliveryAddressId;

    @Column(name = "is_contactless")
    private boolean isContactless = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "delivery_time", nullable = false)
    private LocalDateTime deliveryTime;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "total_price", nullable = false)
    private double totalPrice;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")  // Указываем колонку в order_items, где хранится FK
    private List<OrderItemEntity> items = new ArrayList<>();

}