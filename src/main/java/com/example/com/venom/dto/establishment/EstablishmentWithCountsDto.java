package com.example.com.venom.dto.establishment;

import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.enums.EstablishmentStatus;
import com.example.com.venom.enums.EstablishmentType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstablishmentWithCountsDto {
    private Long id;
    private String name;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double rating;
    private EstablishmentStatus status;
    private Long createdUserId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfCreation;

    private EstablishmentType type;

    private List<String> photoBase64s;

    private String operatingHoursString;

    private int pendingOrdersCount;
    private int pendingBookingsCount;

    public static EstablishmentWithCountsDto fromEntity(EstablishmentEntity entity, int pendingOrdersCount, int pendingBookingsCount) {
        return new EstablishmentWithCountsDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getAddress(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getRating(),
                entity.getStatus(),
                entity.getCreatedUserId(),
                entity.getDateOfCreation(),
                entity.getType(),
                entity.getPhotoBase64s(),
                entity.getOperatingHoursString(),
                pendingOrdersCount,
                pendingBookingsCount
        );
    }
}