package com.example.com.venom.repository.Menu;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.com.venom.entity.Menu.DrinksGroupEntity;

@Repository
public interface DrinksGroupRepository extends JpaRepository<DrinksGroupEntity, Long> {
    /** Найти все группы напитков, принадлежащие определенному заведению. */
    List<DrinksGroupEntity> findByEstablishmentId(Long establishmentId);
}