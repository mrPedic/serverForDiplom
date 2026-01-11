package com.example.com.venom.dto.order;

import com.example.com.venom.enums.order.OrderStatus;
import com.example.com.venom.enums.order.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private Long id;
    private Long establishmentId;
    private String establishmentName;
    private Long userId;
    private String userName;
    private OrderStatus status;
    private Long deliveryAddressId;
    private DeliveryAddressDto deliveryAddress;
    private List<OrderItemDto> items;
    private boolean isContactless;
    private PaymentMethod paymentMethod;
    private LocalDateTime deliveryTime;
    private String comments;
    private double totalPrice;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}