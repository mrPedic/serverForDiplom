package com.example.com.venom.dto;

import java.time.LocalDate;
import java.util.List;

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
    private Double latitude; 
    private Double longitude; 
    private Double rating;
    private EstablishmentStatus status;
    private Long createdUserId; 

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfCreation;
    
    private EstablishmentType type; 

    private List<String> photoBase64s; 
    
    // Метод-фабрика для преобразования Entity в DTO (ОБНОВЛЕН)
    public static EstablishmentDisplayDto fromEntity(EstablishmentEntity entity) {
        return new EstablishmentDisplayDto(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getAddress(),
            entity.getLatitude(),  
            entity.getLongitude(), 
            entity.getRating(),
            entity.getStatus(),
            entity.getCreatedUserId(), 
            entity.getDateOfCreation(),
            entity.getType(),
            entity.getPhotoBase64s()
        );
    }
}