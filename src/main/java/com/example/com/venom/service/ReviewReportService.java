// ReviewReportService.java
package com.example.com.venom.service;

import com.example.com.venom.dto.ReviewReportDto;
import com.example.com.venom.entity.ReviewReportEntity;
import com.example.com.venom.repository.ReviewReportRepository;
import com.example.com.venom.repository.ReviewRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewReportService {

    private final ReviewReportRepository repository;
    private final ReviewRepository reviewRepository;

    public ReviewReportService(ReviewReportRepository repository, ReviewRepository reviewRepository) {
        this.repository = repository;
        this.reviewRepository = reviewRepository;
    }

    public void createReport(ReviewReportDto dto) {
        ReviewReportEntity entity = new ReviewReportEntity();
        BeanUtils.copyProperties(dto, entity);
        repository.save(entity);
    }

    public List<ReviewReportDto> getAllReports() {
        return repository.findAll().stream()
                .filter(entity -> "PENDING".equals(entity.getStatus())) // Фильтруем только PENDING жалобы
                .map(entity -> {
                    ReviewReportDto dto = new ReviewReportDto();
                    BeanUtils.copyProperties(entity, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void resolveReport(Long reportId) {
        repository.findById(reportId).ifPresent(report -> {
            // 1. Удаляем сам отзыв из базы
            reviewRepository.deleteById(report.getReviewId());
            // 2. Удаляем жалобу, так как она отработана
            repository.delete(report);
        });
    }

    public void deleteReport(Long reportId) {
        repository.deleteById(reportId);
    }
}