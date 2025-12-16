// src/main/java/com/example/com/venom/dto/booking/OwnerBookingDisplayDto.java
package com.example.com.venom.dto.booking;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

// OwnerBookingDisplayDto.java — упрощённая версия
@Data
@Builder
public class OwnerBookingDisplayDto {
    private Long id;
    private Long establishmentId;
    private String establishmentName;
    private String userName;           // имя из аккаунта
    private String guestPhone;         // ← только этот телефон показываем!
    private String tableName;
    private Integer numberOfGuests;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
}