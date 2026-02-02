package com.example.com.venom.dto;

import lombok.Data;

@Data
public class ReviewReportDto {
    private Long id;
    private Long reviewId;
    private Long userId;
    private Long establishmentId;
    private String reason;
    private String description;
}