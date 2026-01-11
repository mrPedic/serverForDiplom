package com.example.com.venom.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeliveryAddressRequest {
    @NotBlank(message = "Улица обязательна")
    private String street;

    @NotBlank(message = "Дом обязателен")
    private String house;

    private String building;

    @NotBlank(message = "Квартира обязательна")
    private String apartment;

    private String entrance;
    private String floor;
    private String comment;
    private boolean isDefault = false;
}