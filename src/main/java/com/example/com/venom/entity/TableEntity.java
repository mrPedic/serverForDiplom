package com.example.com.venom.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tables")
@Getter
@Setter
@NoArgsConstructor
public class TableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID заведения, которому принадлежит столик. 
    // Используем простой Long для привязки, как и планировалось
    @Column(name = "establishment_id", nullable = false)
    private Long establishmentId; 

    // Название или номер столика (например, "Столик №1", "У окна")
    @Column(name = "table_name", nullable = false)
    private String name; 

    // Описание (например, "Для большой компании", "У камина")
    @Column(columnDefinition = "TEXT")
    private String description; 

    // Максимальное количество человек
    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

}