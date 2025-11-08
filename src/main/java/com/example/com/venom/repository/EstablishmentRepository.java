package com.example.com.venom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.entity.EstablishmentStatus;

public interface EstablishmentRepository extends JpaRepository<EstablishmentEntity, Long> {

    // 1. Поиск по точному совпадению названия и адреса (для проверки уникальности)
    @Query(value = "SELECT * FROM establishments WHERE \"name\" = :name AND \"address\" = :address LIMIT 1", nativeQuery = true)
    Optional<EstablishmentEntity> findByNameAndAddress(@Param("name") String name, @Param("address") String address);

    // 2. Поиск по части названия (используется в старых версиях, заменен на п.3)
    // Оставляем для совместимости, но LIMIT 1 может быть нежелателен для поиска.
    @Query(value = "SELECT * FROM establishments WHERE LOWER(\"name\") LIKE LOWER(CONCAT('%', :name, '%')) LIMIT 1", nativeQuery = true)
    Optional<EstablishmentEntity> findByName(@Param("name") String name);

    // 3. ⭐ ПОИСК: По части названия ИЛИ части адреса (без учета регистра, для экрана поиска)
    @Query(value = 
    	"SELECT * FROM establishments " +
    	"WHERE LOWER(\"name\") LIKE LOWER(CONCAT('%', :query, '%')) " +
    	"OR LOWER(\"address\") LIKE LOWER(CONCAT('%', :query, '%'))", 
    	nativeQuery = true)
    List<EstablishmentEntity> searchByNameOrAddress(@Param("query") String query);
    
    // 4. Поиск по ID пользователя, создавшего заведение
    @Query(value = "SELECT * FROM establishments WHERE \"created_user_id\" = :userId", nativeQuery = true)
    List<EstablishmentEntity> findByCreatedUserId(@Param("userId") Long createdUserId);

    // 5. Поиск по статусу (для панели администратора - "Pending" или для отображения "Approved")
    // NOTE: В SQL/JPQL мы должны использовать enum.name() или .ordinal() если статус хранится как INT/STRING. 
    // Предполагая, что статус хранится как строка (TEXT/VARCHAR) с именем ENUM.
    // @Query(value = "SELECT * FROM establishments WHERE \"status\" = :status", nativeQuery = true)
    List<EstablishmentEntity> findByStatus(@Param("status") EstablishmentStatus status);
}