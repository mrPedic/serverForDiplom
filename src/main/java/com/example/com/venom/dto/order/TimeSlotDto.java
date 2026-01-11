package com.example.com.venom.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotDto {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String displayText; // "12:30-13:00"
    private boolean isAvailable;
}