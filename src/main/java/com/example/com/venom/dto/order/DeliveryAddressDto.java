package com.example.com.venom.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddressDto {
    private Long id;
    private Long userId;
    private String street;
    private String house;
    private String building;
    private String apartment;
    private String entrance;
    private String floor;
    private String comment;
    private boolean isDefault;
    private LocalDateTime createdAt;
}