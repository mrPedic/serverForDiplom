package com.example.com.venom.enums.order;

public enum OrderStatus {
    PENDING,      // На рассмотрении
    CONFIRMED,    // Подтвержден
    IN_PROGRESS,  // В процессе приготовления
    OUT_FOR_DELIVERY, // В доставке
    DELIVERED,    // Доставлен
    CANCELLED,    // Отменен
    REJECTED      // Отклонен заведением
}