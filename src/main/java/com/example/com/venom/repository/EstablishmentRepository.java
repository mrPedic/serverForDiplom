package com.example.com.venom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.entity.EstablishmentStatus;
import com.example.com.venom.entity.EstablishmentType; // ⭐ ИМПОРТ ДЛЯ JPQL

public interface EstablishmentRepository extends JpaRepository<EstablishmentEntity, Long> {

  // 1. Поиск по точному совпадению (остается)
  @Query(value = "SELECT * FROM establishments WHERE \"name\" = :name AND \"address\" = :address LIMIT 1", nativeQuery = true)
  Optional<EstablishmentEntity> findByNameAndAddress(@Param("name") String name, @Param("address") String address);

  // 2. (УДАЛЕНО) Старый метод findByName (LIMIT 1)

  // 3. (УДАЛЕНО) Старый nativeQuery searchByNameOrAddress (конфликтовал)
    
  // 4. Поиск по ID пользователя (остается)
@Query(value = "SELECT * FROM establishments WHERE \"created_user_id\" = :userId", nativeQuery = true)
  List<EstablishmentEntity> findByCreatedUserId(@Param("userId") Long createdUserId);

 // 5. Поиск по статусу (остается)
 List<EstablishmentEntity> findByStatus(@Param("status") EstablishmentStatus status);

    // ========================== НОВЫЕ МЕТОДЫ ПОИСКА (JPQL) ==========================
    // (Используем JPQL (не native), так как он лучше работает с Enum и списками)

    /**
     * Поиск по тексту (БЕЗ фильтра по типу)
     */
 @Query("SELECT e FROM EstablishmentEntity e WHERE " +
    "LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.address) LIKE LOWER(CONCAT('%', :query, '%'))")
  List<EstablishmentEntity> searchByNameOrAddress(
    @Param("query") String query
  );

    /**
     * Поиск по тексту И С фильтром по типу
     */
  @Query("SELECT e FROM EstablishmentEntity e WHERE " +
     "(LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(e.address) LIKE LOWER(CONCAT('%', :query, '%'))) " +
     "AND e.type IN :types") 
  List<EstablishmentEntity> searchByNameOrAddressAndType(
    @Param("query") String query,
    @Param("types") List<EstablishmentType> types // ⭐ Тип Enum
  );
}