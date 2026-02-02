package com.example.com.venom.controller;

import com.example.com.venom.dto.ReviewReportDto;
import com.example.com.venom.repository.ReviewRepository;
import com.example.com.venom.service.ReviewReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ReviewReportController {

    private final ReviewReportService service;

    public ReviewReportController(ReviewReportService service) {
        this.service = service;
    }

    @PostMapping("/reviews/{reviewId}/report")
    public ResponseEntity<Void> reportReview(@PathVariable Long reviewId, @RequestBody ReviewReportDto dto) {
        dto.setReviewId(reviewId); // Устанавливаем reviewId из пути
        service.createReport(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/reports/reviews")
    public List<ReviewReportDto> getReviewReports() {
        return service.getAllReports();
    }

    @PutMapping("/admin/reports/{reportId}/resolve")
    public ResponseEntity<Void> resolveReviewReport(@PathVariable Long reportId) {
        service.resolveReport(reportId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/admin/reports/{reportId}")
    public ResponseEntity<Void> deleteReviewReport(@PathVariable Long reportId) {
        service.deleteReport(reportId);
        return ResponseEntity.ok().build();
    }


}