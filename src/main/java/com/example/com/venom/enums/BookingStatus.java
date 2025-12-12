package com.example.com.venom.enums;

/**
 * Перечисление для статуса бронирования.
 * Оно должно быть определено как отдельный public enum.
 */
public enum BookingStatus {
    PENDING,    // Ожидает подтверждения
    CONFIRMED,  // Подтверждено
    CANCELLED,  // Отменено
    COMPLETED,  // Завершено (после того, как клиент посетил заведение)
    NO_SHOW     // Клиент не пришел
}