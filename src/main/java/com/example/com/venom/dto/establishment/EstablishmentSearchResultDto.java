package com.example.com.venom.dto.establishment;

import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.enums.EstablishmentType;

import lombok.Value;

/**
 * Облегченный DTO для отображения заведений в результатах поиска.
 */
@Value
public class EstablishmentSearchResultDto {
    Long id;
    String name;
    String address;
    Double rating;
    EstablishmentType type;

    /**
     * Статический метод для преобразования Entity в DTO.
     */
    public static EstablishmentSearchResultDto fromEntity(EstablishmentEntity entity) {
        return new EstablishmentSearchResultDto(
            entity.getId(),
            entity.getName(),
            entity.getAddress(),
            entity.getRating(),
            entity.getType()
        );
    }
}