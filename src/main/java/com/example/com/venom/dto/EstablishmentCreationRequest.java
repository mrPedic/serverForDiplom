package com.example.com.venom.dto;

import java.util.List;

import com.example.com.venom.entity.EstablishmentType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для приема данных при создании нового заведения с клиента.
 * Используем Strong Typing (Double, Long, EstablishmentStatus) для соответствия клиентскому Kotlin Entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstablishmentCreationRequest {
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Long createdUserId;
    private String description;
    private EstablishmentType type;
    private List<String> photoBase64s;
}

// Enum EstablishmentStatus не был предоставлен в Java, но он нужен для десериализации клиента.
// Предполагаем, что он находится в com.example.com.venom.entity

