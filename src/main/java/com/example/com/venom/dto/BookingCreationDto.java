package com.example.com.venom.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class BookingCreationDto {
    private Long userId;
    private Long establishmentId;
    private Long tableId;
    private LocalDateTime startTime;
    private Integer numPeople;
    private String notes;
}