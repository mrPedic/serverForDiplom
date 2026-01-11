package com.example.com.venom.controller.order;

import com.example.com.venom.dto.order.CreateDeliveryAddressRequest;
import com.example.com.venom.dto.order.DeliveryAddressDto;
import com.example.com.venom.service.order.DeliveryAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delivery-addresses")
@RequiredArgsConstructor
@Validated
public class DeliveryAddressController {

    private final DeliveryAddressService deliveryAddressService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DeliveryAddressDto>> getUserAddresses(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        validateUserAccess(userId, userDetails);

        List<DeliveryAddressDto> addresses = deliveryAddressService.getUserAddresses(userId);
        return ResponseEntity.ok(addresses);
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<DeliveryAddressDto> createAddress(
            @PathVariable Long userId,
            @Valid @RequestBody CreateDeliveryAddressRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        validateUserAccess(userId, userDetails);

        DeliveryAddressDto address = deliveryAddressService.createAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }

    @PutMapping("/{addressId}/user/{userId}")
    public ResponseEntity<DeliveryAddressDto> updateAddress(
            @PathVariable Long addressId,
            @PathVariable Long userId,
            @Valid @RequestBody CreateDeliveryAddressRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        validateUserAccess(userId, userDetails);

        DeliveryAddressDto address = deliveryAddressService.updateAddress(userId, addressId, request);
        return ResponseEntity.ok(address);
    }

    @DeleteMapping("/{addressId}/user/{userId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long addressId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        validateUserAccess(userId, userDetails);

        deliveryAddressService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{addressId}/user/{userId}/set-default")
    public ResponseEntity<Void> setDefaultAddress(
            @PathVariable Long addressId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        validateUserAccess(userId, userDetails);

        deliveryAddressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok().build();
    }

    private void validateUserAccess(Long userId, UserDetails userDetails) {
        // Проверка, что пользователь имеет доступ к этому ресурсу
        // Реализация зависит от вашей системы аутентификации
    }
}