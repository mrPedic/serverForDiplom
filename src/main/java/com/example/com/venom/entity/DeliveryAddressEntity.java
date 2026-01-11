package com.example.com.venom.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_addresses")
@Setter
@Getter
public class DeliveryAddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String house;

    private String building; // корпус/строение

    @Column(nullable = false)
    private String apartment;

    private String entrance; // подъезд

    private String floor; // этаж

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "is_default")
    private boolean isDefault = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        address.append(street).append(", д. ").append(house);
        if (building != null && !building.isEmpty()) {
            address.append(", корпус ").append(building);
        }
        if (apartment != null && !apartment.isEmpty()) {
            address.append(", кв. ").append(apartment);
        }
        if (entrance != null && !entrance.isEmpty()) {
            address.append(", подъезд ").append(entrance);
        }
        if (floor != null && !floor.isEmpty()) {
            address.append(", этаж ").append(floor);
        }
        return address.toString();
    }
}