package com.example.com.venom.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_reports")
@Data
public class ReviewReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reviewId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long establishmentId;

    @Column(nullable = false)
    private String reason;

    private String description;

    private LocalDateTime createdAt = LocalDateTime.now();

    private String status = "PENDING"; // PENDING, RESOLVED
}