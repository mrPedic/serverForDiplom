package com.example.com.venom;

/**
 * Перечисление для статуса бронирования.
 * Оно должно быть определено как отдельный public enum.
 */
public enum StatusForBooking {
    PENDING,    // Ожидает подтверждения
    CONFIRMED,  // Подтверждено
    CANCELLED,  // Отменено
    COMPLETED,  // Завершено (после того, как клиент посетил заведение)
    NO_SHOW     // Клиент не пришел
}