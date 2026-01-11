package com.example.com.venom.dto.establishment;

import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.enums.EstablishmentType;

import lombok.Value;

/**
 * Облегченный DTO для отображения заведений на карте (маркеры).
 * Содержит только минимально необходимые данные для оптимизации загрузки.
 */
@Value
public class EstablishmentMarkerDto {
    Long id;
    String name;
    String address;
    Double latitude;
    Double longitude;
    EstablishmentType type;
    Double rating;
    String operatingHoursString; 

    /**
     * Статический метод для преобразования Entity в DTO.
     */
    public static EstablishmentMarkerDto fromEntity(EstablishmentEntity entity) {
        return new EstablishmentMarkerDto(
            entity.getId(),
            entity.getName(),
            entity.getAddress(),
            entity.getLatitude(),
            entity.getLongitude(),
            entity.getType(),
            entity.getRating(),
            entity.getOperatingHoursString()
        );
    }
}
