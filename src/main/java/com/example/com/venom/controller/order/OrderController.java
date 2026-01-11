package com.example.com.venom.controller.order;

import com.example.com.venom.dto.order.CreateOrderRequest;
import com.example.com.venom.dto.order.OrderDto;
import com.example.com.venom.dto.order.TimeSlotDto;
import com.example.com.venom.dto.order.UpdateOrderStatusRequest;
import com.example.com.venom.entity.OrderEntity;
import com.example.com.venom.enums.order.OrderStatus;
import com.example.com.venom.repository.order.OrderRepository;
import com.example.com.venom.service.order.OrderService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserIdFromPrincipal(userDetails);
        OrderDto order = orderService.createOrder(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto>> getUserOrders(
            @PathVariable Long userId,
            @RequestParam(required = false) OrderStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {

        validateUserAccess(userId, userDetails);

        List<OrderDto> orders;
        if (status != null) {
            // Фильтрация по статусу
            orders = orderService.getUserOrders(userId).stream()
                    .filter(order -> order.getStatus() == status)
                    .collect(Collectors.toList());
        } else {
            orders = orderService.getUserOrders(userId);
        }

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/establishment/{establishmentId}")
    public ResponseEntity<List<OrderDto>> getEstablishmentOrders(
            @PathVariable Long establishmentId,
            @RequestParam(required = false) OrderStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Проверяем, что пользователь является владельцем заведения
        validateEstablishmentAccess(establishmentId, userDetails);

        List<OrderDto> orders = orderService.getEstablishmentOrders(establishmentId, status);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Проверяем, что пользователь является владельцем заведения
        validateOrderAccess(orderId, userDetails);

        OrderDto order = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}/time-slots")
    public ResponseEntity<List<TimeSlotDto>> getAvailableTimeSlots(
            @PathVariable Long orderId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));

        List<TimeSlotDto> timeSlots = orderService.getAvailableTimeSlots(
                order.getEstablishment().getId(),
                date
        );

        return ResponseEntity.ok(timeSlots);
    }

    Long getUserIdFromPrincipal(UserDetails userDetails) {
        // Извлечение ID пользователя из UserDetails
        // Реализация зависит от вашей системы аутентификации
        return Long.parseLong(userDetails.getUsername());
    }

    void validateUserAccess(Long userId, UserDetails userDetails) {
        Long currentUserId = getUserIdFromPrincipal(userDetails);
        if (!currentUserId.equals(userId)) {
            throw new IllegalArgumentException("Доступ запрещен");
        }
    }

    void validateEstablishmentAccess(Long establishmentId, UserDetails userDetails) {
        // Проверка, что пользователь является владельцем заведения
        // Реализация зависит от вашей системы аутентификации
    }

    void validateOrderAccess(Long orderId, UserDetails userDetails) {
        // Проверка, что пользователь имеет доступ к заказу
        // Реализация зависит от вашей системы аутентификации
    }
}