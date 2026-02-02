package com.example.com.venom.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "admin_saved_queries")
@Data
public class AdminQueryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;       // Название, например "Топ 10 должников"
    private String description; // Описание

    @Column(columnDefinition = "TEXT")
    private String sqlQuery;    // Сам SQL, например "SELECT * FROM users..."
}