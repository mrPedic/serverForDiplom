package com.example.com.venom.dto.order;

import com.example.com.venom.enums.order.PaymentMethod;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    @NotNull(message = "ID заведения обязателен")
    private Long establishmentId;

    private Long deliveryAddressId;
    private DeliveryAddressDto deliveryAddress; // Новый адрес, если не выбран существующий

    @NotEmpty(message = "Список позиций не может быть пустым")
    private List<CreateOrderItemDto> items;

    private boolean isContactless = false;

    @NotNull(message = "Способ оплаты обязателен")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Время доставки обязательно")
    @Future(message = "Время доставки должно быть в будущем")
    private LocalDateTime deliveryTime;

    private String comments;
}