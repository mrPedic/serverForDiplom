package com.example.com.venom.controller.order;

import com.example.com.venom.dto.order.DeliveryAddressDto;
import com.example.com.venom.service.order.DeliveryAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/delivery-addresses")  // 👈 Измените путь!
@RequiredArgsConstructor
@Validated
public class DeliveryAddressController {

    private final DeliveryAddressService deliveryAddressService;

    @GetMapping
    public ResponseEntity<List<DeliveryAddressDto>> getUserAddresses(
            @PathVariable Long userId) {
        List<DeliveryAddressDto> addresses = deliveryAddressService.getUserAddresses(userId);
        return ResponseEntity.ok(addresses);
    }

    @PostMapping
    public ResponseEntity<DeliveryAddressDto> createAddress(
            @PathVariable Long userId,
            @Valid @RequestBody DeliveryAddressDto request) {  // 👈 Измените тип параметра
        // Преобразуем DeliveryAddressDto в CreateDeliveryAddressRequest
        DeliveryAddressDto createdAddress = deliveryAddressService.createAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAddress);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<DeliveryAddressDto> updateAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId,
            @Valid @RequestBody DeliveryAddressDto request) {  // 👈 Измените тип параметра
        DeliveryAddressDto updatedAddress = deliveryAddressService.updateAddress(userId, addressId, request);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId) {
        deliveryAddressService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{addressId}/set-default")
    public ResponseEntity<Void> setDefaultAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId) {
        deliveryAddressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok().build();
    }
}