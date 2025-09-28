package com.example.com.venom.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "establishments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstablishmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Double latitude;

    private Double longitude;

    private String address;

    private String description;

    private Double rating;

    @Column(columnDefinition = "jsonb")
    private String menuJson;
}
