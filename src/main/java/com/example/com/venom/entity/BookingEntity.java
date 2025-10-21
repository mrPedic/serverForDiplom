package com.example.com.venom.entity;

import java.time.LocalDateTime;

import com.example.com.venom.StatusForBooking;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "booking")
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Внешние ключи
    private Long userId;
    private Long establishmentId;
    private Long tableId; // Внешний ключ на конкретный столик (если применимо)

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Фактическое количество человек в брони
    private Integer numPeople;

    /**
     * Поле для хранения статуса бронирования.
     * @Enumerated(EnumType.STRING) гарантирует, что в БД будет храниться строковое имя статуса
     * (например, "CONFIRMED"), а не его порядковый номер, что намного надежнее.
     */
    @Enumerated(EnumType.STRING)
    private StatusForBooking status;

    // Опционально: поле для заметок или специальных пожеланий клиента
    private String notes;

    // -------------------------------------------------------------------------
    // Добавьте геттеры и сеттеры (и, возможно, конструкторы) здесь
    // -------------------------------------------------------------------------
    
    // Пример минимальных геттеров
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}