package com.example.com.venom.dto.booking;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class BookingCreationDto {
    private Long userId;
    private Long establishmentId;
    private Long tableId;
    private Long durationMinutes;
    private LocalDateTime startTime;
    private Integer numPeople;
    private String notes;
    private String guestPhone;
}