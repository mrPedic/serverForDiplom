package com.example.com.venom.dto.order;

import com.example.com.venom.enums.order.OrderStatus;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    private OrderStatus status;
    private String rejectionReason;
}