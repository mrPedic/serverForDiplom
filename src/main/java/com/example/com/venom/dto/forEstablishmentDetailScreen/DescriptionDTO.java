package com.example.com.venom.dto.forEstablishmentDetailScreen;

import java.time.LocalDate;

import com.example.com.venom.entity.EstablishmentType;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DescriptionDTO {
    private String name;
    private String description;
    private String address;
    private Double rating;
    private EstablishmentType type;
    private String operatingHoursString;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfCreation;
}