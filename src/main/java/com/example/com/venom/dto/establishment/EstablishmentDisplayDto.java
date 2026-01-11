package com.example.com.venom.dto.establishment;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.enums.EstablishmentStatus;
import com.example.com.venom.enums.EstablishmentType;
import com.fasterxml.jackson.annotation.JsonFormat; // <-- ИМПОРТ

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

    private String operatingHoursString;

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
            Collections.emptyList(),
            entity.getOperatingHoursString()
        );
    }
}