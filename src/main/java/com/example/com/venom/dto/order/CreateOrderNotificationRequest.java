package com.example.com.venom.dto.order;

import com.example.com.venom.enums.order.OrderNotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderNotificationRequest {
    @NotNull
    private Long orderId;

    @NotNull
    private Long userId;

    @NotNull
    private Long establishmentId;

    @NotNull
    private OrderNotificationType type;

    @NotBlank
    private String message;
}