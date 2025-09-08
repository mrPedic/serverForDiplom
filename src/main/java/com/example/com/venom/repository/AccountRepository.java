package com.example.com.venom.repository;

import com.example.com.venom.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    /* 
    // Поиск по логину
    Optional<AccountEntity> findByLogin(String login);
    
    // Поиск по имени
    List<AccountEntity> findByName(String name);
    
    // Поиск по роли
    List<AccountEntity> findByRole(AccountEntity.Role role);
    
    // Поиск по имени (содержит подстроку)
    List<AccountEntity> findByNameContainingIgnoreCase(String name);
    
    // Поиск по логину (содержит подстроку)
    List<AccountEntity> findByLoginContainingIgnoreCase(String login);
    
    // Проверка существования логина
    boolean existsByLogin(String login);
    
    // Кастомный запрос для поиска по имени и роли
    @Query("SELECT a FROM AccountEntity a WHERE a.name LIKE %:name% AND a.role = :role")
    List<AccountEntity> findByNameAndRole(@Param("name") String name, @Param("role") AccountEntity.Role role);

    */
}
