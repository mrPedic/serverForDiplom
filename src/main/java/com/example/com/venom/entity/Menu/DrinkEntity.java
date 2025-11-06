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

// Сущность Напитка (DrinkEntity)
// Соответствует классу Drink, хранит общие данные и имеет список опций
// (связь One-to-Many будет обрабатываться в Service/Repository).
@Entity
@Table(name = "menu_drink")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrinkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ⭐ Внешний ключ: к какой группе напитков относится (FK на DrinksGroupEntity)
    private Long drinkGroupId; 
    
    private String name;
    private Double caloriesPer100g;
    private Double fatPer100g;
    private Double carbohydratesPer100g;
    private Double proteinPer100g;
    private String ingredients;
    
    @Lob
    private String photoBase64;
    
    // *Примечание:* Поле 'options' здесь отсутствует, так как в JPA 
    // более эффективно загружать опции через репозиторий DrinkOptionRepository, 
    // используя drinkId в DrinkService.
}