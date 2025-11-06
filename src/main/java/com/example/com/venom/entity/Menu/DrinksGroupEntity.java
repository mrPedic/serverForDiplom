package com.example.com.venom.entity.Menu;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Сущность Группы Напитков (DrinksGroupEntity)
// Соответствует классу DrinksGroup.
// -------------------------------------------------------------------
@Entity
@Table(name = "menu_drink_group")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrinksGroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ⭐ Внешний ключ: к какому заведению относится (FK на EstablishmentEntity)
    private Long establishmentId; 
    
    private String name;
}