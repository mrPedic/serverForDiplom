package com.example.com.venom.entity;

import com.example.com.venom.enums.order.MenuItemType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

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

    @Column(columnDefinition = "JSON")
    private String options; // JSON с выбранными опциями (для напитков)
}