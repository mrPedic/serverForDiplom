package com.example.com.venom.dto;

import lombok.Data;

@Data
public class TableCreationDto {
    private String name; 
    private String description; 
    private Integer maxCapacity; 
}