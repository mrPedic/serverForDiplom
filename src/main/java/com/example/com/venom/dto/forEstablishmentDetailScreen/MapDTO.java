package com.example.com.venom.dto.forEstablishmentDetailScreen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MapDTO {
    private Double latitude;
    private Double longitude;
    private String address;
}