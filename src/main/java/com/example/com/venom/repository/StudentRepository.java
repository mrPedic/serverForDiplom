package com.example.com.venom.repository;

import com.example.com.venom.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, Long> {
    /* 
    // Поиск по имени (точное совпадение)
    Optional<StudentEntity> findByName(String name);
    
    // Поиск по возрасту
    List<StudentEntity> findByAge(Integer age);
    
    // Поиск студентов старше определенного возраста
    List<StudentEntity> findByAgeGreaterThan(Integer age);
    
    // Поиск студентов младше определенного возраста
    List<StudentEntity> findByAgeLessThan(Integer age);
    
    // Поиск по имени (содержит подстроку)
    List<StudentEntity> findByNameContainingIgnoreCase(String name);
    
    // Поиск студентов в определенном возрастном диапазоне
    List<StudentEntity> findByAgeBetween(Integer minAge, Integer maxAge);
    
    // Кастомный SQL запрос
    @Query("SELECT s FROM StudentEntity s WHERE s.name LIKE %:name% AND s.age > :minAge")
    List<StudentEntity> findStudentsByNameAndMinAge(@Param("name") String name, @Param("minAge") Integer minAge);

    */
}
