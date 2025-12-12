package com.example.com.venom.dto;

import java.util.List;

import com.example.com.venom.enums.EstablishmentStatus;
import com.example.com.venom.enums.EstablishmentType;

import lombok.Data;

@Data
public class EstablishmentUpdateRequest {
    private String name;
    private String description;
    private String address;
    private Double latitude; 
    private Double longitude; 
    private EstablishmentStatus status;
    private EstablishmentType type; 
    private List<String> photoBase64s; 
    
    // ⭐ ВАЖНОЕ ПОЛЕ
    private String operatingHoursString; 
}