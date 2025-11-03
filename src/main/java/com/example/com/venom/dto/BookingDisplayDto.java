package com.example.com.venom.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * DTO для отображения информации о бронировании на экране пользователя.
 * Серверная версия.
 */
@Data
@Builder
public class BookingDisplayDto {
    private Long id;
    private String establishmentName;
    private String establishmentAddress;
    private Double establishmentLatitude;
    private Double establishmentLongitude;
    private String tableName;
    private Integer tableMaxCapacity;
    private LocalDateTime startTime;
    private Long durationMinutes; 
    private String status; 
}