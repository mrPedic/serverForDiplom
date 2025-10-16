package com.example.com.venom.dto;

import java.time.LocalDate;

import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.entity.EstablishmentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO, используемый для отображения списка заведений.
 * Контролирует, какие поля отправляются клиенту, и обеспечивает корректный формат даты (ISO String).
 */
@Data
@AllArgsConstructor
public class EstablishmentDisplayDto {
    private Long id;
    private String name;
    private String description;
    private String address;
    private Double rating;
    private EstablishmentStatus status;

    // Обеспечиваем, что дата сериализуется как строка "yyyy-MM-dd"
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfCreation;
    
    // Метод-фабрика для преобразования Entity в DTO
    public static EstablishmentDisplayDto fromEntity(EstablishmentEntity entity) {
        return new EstablishmentDisplayDto(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getAddress(),
            entity.getRating(),
            entity.getStatus(),
            entity.getDateOfCreation()
        );
    }
}
