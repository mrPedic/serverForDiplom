package com.example.com.venom.repository;

import com.example.com.venom.entity.AdminQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminQueryRepository extends JpaRepository<AdminQueryEntity, Long> {
}