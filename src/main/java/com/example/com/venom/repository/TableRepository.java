package com.example.com.venom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.com.venom.entity.TableEntity;

@Repository
public interface TableRepository extends JpaRepository<TableEntity, Long> {
    // Метод для получения всех столиков заведения (понадобится для бронирования)
    List<TableEntity> findByEstablishmentId(Long establishmentId);
}