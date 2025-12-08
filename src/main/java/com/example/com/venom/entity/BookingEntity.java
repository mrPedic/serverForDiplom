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
import lombok.AllArgsConstructor;          // Добавляем Lombok Data
import lombok.Data; // Добавляем конструктор без аргументов
import lombok.NoArgsConstructor; // Добавляем конструктор со всеми аргументами

@Entity
@Table(name = "booking")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Внешние ключи
    private Long userId;
    private Long establishmentId;
    private Long tableId; // Внешний ключ на конкретный столик (если применимо)

    private LocalDateTime startTime;
    private LocalDateTime endTime; // Мы будем рассчитывать это на основе startTime + стандартной длительности

    // Фактическое количество человек в брони
    private Integer numPeople;

    /**
     * Поле для хранения статуса бронирования.
     */
    @Enumerated(EnumType.STRING)
    private StatusForBooking status;

    // Опционально: поле для заметок или специальных пожеланий клиента
    private String notes;
}