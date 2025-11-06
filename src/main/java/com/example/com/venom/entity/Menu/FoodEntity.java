package com.example.com.venom.entity.Menu;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 1. Сущность Блюда (Food)
 */
@Entity
@Table(name = "menu_food")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Внешний ключ: к какой группе относится
    private Long foodGroupId; 
    
    private String name;
    private Double caloriesPer100g;
    private Double fatPer100g;
    private Double carbohydratesPer100g;
    private Double proteinPer100g;
    private String ingredients;
    private Double cost;
    private Integer weight;
    
    @Lob // Для больших данных, таких как Base64 строка изображения
    private String photoBase64;
}