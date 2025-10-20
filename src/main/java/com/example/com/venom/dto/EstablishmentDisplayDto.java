package com.example.com.venom.dto;

import java.time.LocalDate;

import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.entity.EstablishmentStatus;
import com.example.com.venom.entity.EstablishmentType; // <-- ИМПОРТ
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EstablishmentDisplayDto {
    private Long id;
    private String name;
    private String description;
    private String address;
    private Double latitude; // <-- ДОБАВЛЕНО для отображения на карте
    private Double longitude; // <-- ДОБАВЛЕНО для отображения на карте
    private Double rating;
    private EstablishmentStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfCreation;
    
    // ⭐ НОВОЕ ПОЛЕ ДЛЯ ФРОНТА
    private EstablishmentType type; 
    
    // Метод-фабрика для преобразования Entity в DTO (ОБНОВЛЕН)
    public static EstablishmentDisplayDto fromEntity(EstablishmentEntity entity) {
        return new EstablishmentDisplayDto(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getAddress(),
            entity.getLatitude(),  // <-- ДОБАВЛЕНО
            entity.getLongitude(), // <-- ДОБАВЛЕНО
            entity.getRating(),
            entity.getStatus(),
            entity.getDateOfCreation(),
            entity.getType() // <-- НОВОЕ ПОЛЕ
        );
    }
}