package com.example.com.venom.entity;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "establishments")
@Getter // Геттеры для всех полей
@Setter // Сеттеры для всех полей
@NoArgsConstructor // Конструктор без аргументов (требуется JPA)
public class EstablishmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Double latitude;

    private Double longitude;

    private String address;

    private String description;

    private Double rating = 0.0;

    private Long idMenu;

    @Column(name = "created_user_id")
    private Long createdUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EstablishmentStatus status;

    @Enumerated(EnumType.STRING)
    private EstablishmentType type; 

    @Column(name="date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfCreation = LocalDate.now();

    // Коллекция для фото (List<String> — ElementCollection)
    @ElementCollection 
    @CollectionTable(
        name = "establishment_photos", 
        joinColumns = @JoinColumn(name = "establishment_id")
    )
    @Column(name = "base64_photo", columnDefinition = "TEXT")
    private List<String> photoBase64s;

    // ✅ НОВОЕ РЕШЕНИЕ: Время работы как строка в основной таблице
    @Column(name = "operating_hours_str", columnDefinition = "TEXT")
    private String operatingHoursString;
    
    // Примечание: Убедитесь, что ваш Spring Boot настроен на автоматическое обновление схемы
    // (например, spring.jpa.hibernate.ddl-auto=update), чтобы этот новый столбец 
    // `operating_hours_str` был добавлен в таблицу `establishments`.
}