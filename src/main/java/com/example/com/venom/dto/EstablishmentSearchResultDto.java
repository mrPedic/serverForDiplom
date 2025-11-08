package com.example.com.venom.dto;

import com.example.com.venom.entity.EstablishmentEntity;

import lombok.Value;

/**
 * Облегченный DTO для отображения заведений в результатах поиска.
 * Содержит только id, name, address и rating.
 */
@Value
public class EstablishmentSearchResultDto {
    Long id;
    String name;
    String address;
    Double rating;

    /**
     * Статический метод для преобразования Entity в DTO.
     */
    public static EstablishmentSearchResultDto fromEntity(EstablishmentEntity entity) {
        return new EstablishmentSearchResultDto(
            entity.getId(),
            entity.getName(),
            entity.getAddress(),
            entity.getRating()
        );
    }
}