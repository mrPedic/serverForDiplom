package com.example.com.venom.dto.order;

import com.example.com.venom.enums.order.PaymentMethod;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateOrderRequest {
    private Long establishmentId;

    // Добавляем это поле, чтобы спастись от NullPointerException, если нет токена
    private Long userId;

    private Long deliveryAddressId;
    private DeliveryAddressDto deliveryAddress; // Если адрес новый
    private List<CreateOrderItemDto> items;
    private boolean isContactless;
    private PaymentMethod paymentMethod;
    private LocalDateTime deliveryTime;
    private String comments;
}