package com.example.com.venom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.enums.EstablishmentStatus;
import com.example.com.venom.enums.EstablishmentType;

public interface EstablishmentRepository extends JpaRepository<EstablishmentEntity, Long> {

  @Query(value = "SELECT * FROM establishments WHERE \"name\" = :name AND \"address\" = :address LIMIT 1", nativeQuery = true)
  Optional<EstablishmentEntity> findByNameAndAddress(@Param("name") String name, @Param("address") String address);

  @Query(value = "SELECT * FROM establishments WHERE \"created_user_id\" = :userId", nativeQuery = true)
  List<EstablishmentEntity> findByCreatedUserId(@Param("userId") Long createdUserId);

  List<EstablishmentEntity> findByStatus(@Param("status") EstablishmentStatus status);

  /**
   * Поиск по тексту (БЕЗ фильтра по типу)
   */
  @Query("SELECT e FROM EstablishmentEntity e WHERE " +
      "LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
      "LOWER(e.address) LIKE LOWER(CONCAT('%', :query, '%'))")
  List<EstablishmentEntity> searchByNameOrAddress(
      @Param("query") String query);

  /**
   * Поиск по тексту И С фильтром по типу
   */
  @Query("SELECT e FROM EstablishmentEntity e WHERE " +
      "(LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(e.address) LIKE LOWER(CONCAT('%', :query, '%'))) " +
      "AND e.type IN :types")
  List<EstablishmentEntity> searchByNameOrAddressAndType(
      @Param("query") String query,
      @Param("types") List<EstablishmentType> types);

  List<EstablishmentEntity> findByTypeIn(List<EstablishmentType> types);
}