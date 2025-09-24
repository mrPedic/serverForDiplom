package com.example.com.venom.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.com.venom.entity.EstablishmentEntity;

public interface EstabilishmentRepository extends JpaRepository<EstablishmentEntity, Long> {

    @Query(value = "SELECT * FROM establishment WHERE \"Name\" = :name AND \"Address\" = :address LIMIT 1", nativeQuery = true)
    Optional<EstablishmentEntity> findByNameAndAddress(@Param("name") String name, @Param("address") String address);

	@Query(value = "SELECT * FROM establishment WHERE LOWER(\"Name\") LIKE LOWER(CONCAT('%', :name, '%')) LIMIT 1", nativeQuery = true)
	Optional<EstablishmentEntity> findByName(@Param("name") String name);

}