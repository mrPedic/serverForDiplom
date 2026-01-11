package com.example.com.venom.controller.order;

import com.example.com.venom.dto.order.TimeSlotDto;
import com.example.com.venom.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/time-slots")
@RequiredArgsConstructor
public class TimeSlotController {

    private final OrderService orderService;

    @GetMapping("/establishment/{establishmentId}")
    public ResponseEntity<List<TimeSlotDto>> getAvailableTimeSlots(
            @PathVariable Long establishmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<TimeSlotDto> timeSlots = orderService.getAvailableTimeSlots(establishmentId, date);
        return ResponseEntity.ok(timeSlots);
    }
}