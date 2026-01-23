package com.example.com.venom.entity;

import com.example.com.venom.enums.order.MenuItemType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;  // ← Импорт для jsonb (лучше для Postgres)
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;  // ← Импорт для @Type

import java.util.Map;  // ← Если используешь Map

@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)  // Просто колонка с ID заказа, без @ManyToOne
    private Long orderId;

    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId; // ID из меню

    @Column(name = "menu_item_name", nullable = false)
    private String menuItemName;

    @Enumerated(EnumType.STRING)
    @Column(name = "menu_item_type", nullable = false)
    private MenuItemType menuItemType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "price_per_unit", nullable = false)
    private double pricePerUnit;

    @Column(name = "total_price", nullable = false)
    private double totalPrice; // pricePerUnit * quantity

    @Type(JsonBinaryType.class)  // ← Вот это вместо старого, для jsonb
    @Column(columnDefinition = "jsonb")  // ← jsonb лучше, чем json (быстрее индексация)
    private Map<String, String> options;  // ← Измени на Map — Hibernate сам превратит в JSON
}