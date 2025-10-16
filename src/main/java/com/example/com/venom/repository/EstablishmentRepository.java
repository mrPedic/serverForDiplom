package com.example.com.venom.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.com.venom.entity.EstablishmentEntity;

public interface EstablishmentRepository extends JpaRepository<EstablishmentEntity, Long> {

    // ⭐ ИСПРАВЛЕНО: Таблица "establishment" заменена на "establishments"
    @Query(value = "SELECT * FROM establishments WHERE \"name\" = :name AND \"address\" = :address LIMIT 1", nativeQuery = true)
    Optional<EstablishmentEntity> findByNameAndAddress(@Param("name") String name, @Param("address") String address);

    // ⭐ ИСПРАВЛЕНО: Таблица "establishment" заменена на "establishments"
    @Query(value = "SELECT * FROM establishments WHERE LOWER(\"name\") LIKE LOWER(CONCAT('%', :name, '%')) LIMIT 1", nativeQuery = true)
    Optional<EstablishmentEntity> findByName(@Param("name") String name);

}
