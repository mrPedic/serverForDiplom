package com.example.com.venom.repository;

import com.example.com.venom.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

	@Query(value = "SELECT * FROM users WHERE \"login\" = :login LIMIT 1", nativeQuery = true)
	Optional<AccountEntity> findByLogin(@Param("login") String login);

	@Query(value = "SELECT * FROM users WHERE \"login\" = :login AND \"password\" = :password LIMIT 1", nativeQuery = true)
	Optional<AccountEntity> findByLoginAndPassword(@Param("login") String login, @Param("password") String password);
}
