package com.example.com.venom.repository;

import com.example.com.venom.entity.ReviewReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReportEntity, Long> {
}