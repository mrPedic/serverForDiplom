package com.example.com.venom.controller.order;

import com.example.com.venom.dto.order.CreateOrderRequest;
import com.example.com.venom.dto.order.OrderDto;
import com.example.com.venom.dto.order.TimeSlotDto;
import com.example.com.venom.repository.order.OrderRepository;
import com.example.com.venom.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders") // ОСТАВЛЯЕМ КАК ЕСТЬ, это работает для POST
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    // 1. Создание заказа (Работает, код 201)
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId;
        if (userDetails != null) {
            userId = Long.parseLong(userDetails.getUsername());
        } else if (request.getUserId() != null) {
            userId = request.getUserId();
        } else {
            throw new IllegalArgumentException("Не удалось определить пользователя.");
        }

        return new ResponseEntity<>(orderService.createOrder(request, userId), HttpStatus.CREATED);
    }

    // 2. === ДОБАВЛЕННЫЙ МЕТОД: Получение заказа по ID ===
    // Решает ошибку 404 для запроса /api/orders/40
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // 3. Получение заказов по ID пользователя
    // Если здесь 404, значит сервер НЕ перезапустился с последними изменениями
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto>> getOrdersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    @GetMapping("/establishment/{establishmentId}/time-slots")
    public ResponseEntity<List<TimeSlotDto>> getAvailableTimeSlots(
            @PathVariable Long establishmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(orderService.getAvailableTimeSlots(establishmentId, date));
    }

    @GetMapping("/user/my")
    public ResponseEntity<List<OrderDto>> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }
}