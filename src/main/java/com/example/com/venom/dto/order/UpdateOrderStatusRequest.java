package com.example.com.venom.dto.order;

import com.example.com.venom.enums.order.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {
    @NotNull(message = "Статус обязателен")
    private OrderStatus status;

    @Size(max = 500, message = "Причина отказа не должна превышать 500 символов")
    private String rejectionReason;
}