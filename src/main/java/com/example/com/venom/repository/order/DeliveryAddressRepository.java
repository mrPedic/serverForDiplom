package com.example.com.venom.repository.order;

import com.example.com.venom.entity.DeliveryAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddressEntity, Long> {

    List<DeliveryAddressEntity> findByUserIdOrderByIsDefaultDesc(Long userId);

    Optional<DeliveryAddressEntity> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("UPDATE DeliveryAddressEntity d SET d.isDefault = false WHERE d.user.id = :userId AND d.isDefault = true")
    void clearDefaultAddresses(@Param("userId") Long userId);

    boolean existsByUserIdAndIsDefaultTrue(Long userId);
}


