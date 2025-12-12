package com.example.com.venom.enums;

/**
 * Перечисление для статуса заведения. Должно совпадать с клиентским Enum.
 */
public enum EstablishmentStatus {
    // На рассмотрении администрации
    PENDING_APPROVAL,
    // Активно, одобрено
    ACTIVE,
    // Отклонено
    REJECTED,
    // Временно неактивно
    DISABLED
}