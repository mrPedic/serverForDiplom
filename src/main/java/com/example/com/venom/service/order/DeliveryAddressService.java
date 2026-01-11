package com.example.com.venom.service.order;

import com.example.com.venom.dto.order.CreateDeliveryAddressRequest;
import com.example.com.venom.dto.order.DeliveryAddressDto;
import com.example.com.venom.entity.DeliveryAddressEntity;
import com.example.com.venom.entity.UserEntity;
import com.example.com.venom.repository.UserRepository;
import com.example.com.venom.repository.order.DeliveryAddressRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryAddressService {

    private final DeliveryAddressRepository deliveryAddressRepository;
    private final UserRepository userRepository;

    public List<DeliveryAddressDto> getUserAddresses(Long userId) {
        return deliveryAddressRepository.findByUserIdOrderByIsDefaultDesc(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public DeliveryAddressDto createAddress(Long userId, CreateDeliveryAddressRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        // Если устанавливается как адрес по умолчанию, сбрасываем флаги у других адресов
        if (request.isDefault()) {
            deliveryAddressRepository.clearDefaultAddresses(userId);
        }

        DeliveryAddressEntity address = new DeliveryAddressEntity();
        address.setUser(user);
        address.setStreet(request.getStreet());
        address.setHouse(request.getHouse());
        address.setBuilding(request.getBuilding());
        address.setApartment(request.getApartment());
        address.setEntrance(request.getEntrance());
        address.setFloor(request.getFloor());
        address.setComment(request.getComment());
        address.setDefault(request.isDefault());

        DeliveryAddressEntity saved = deliveryAddressRepository.save(address);
        return convertToDto(saved);
    }

    public DeliveryAddressDto updateAddress(Long userId, Long addressId, CreateDeliveryAddressRequest request) {
        DeliveryAddressEntity address = deliveryAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Адрес не найден"));

        // Если устанавливается как адрес по умолчанию, сбрасываем флаги у других адресов
        if (request.isDefault() && !address.isDefault()) {
            deliveryAddressRepository.clearDefaultAddresses(userId);
        }

        address.setStreet(request.getStreet());
        address.setHouse(request.getHouse());
        address.setBuilding(request.getBuilding());
        address.setApartment(request.getApartment());
        address.setEntrance(request.getEntrance());
        address.setFloor(request.getFloor());
        address.setComment(request.getComment());
        address.setDefault(request.isDefault());

        DeliveryAddressEntity updated = deliveryAddressRepository.save(address);
        return convertToDto(updated);
    }

    public void deleteAddress(Long userId, Long addressId) {
        DeliveryAddressEntity address = deliveryAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Адрес не найден"));

        boolean wasDefault = address.isDefault();
        deliveryAddressRepository.delete(address);

        // Если удалили адрес по умолчанию, устанавливаем первый доступный как дефолтный
        if (wasDefault) {
            List<DeliveryAddressEntity> remaining = deliveryAddressRepository.findByUserIdOrderByIsDefaultDesc(userId);
            if (!remaining.isEmpty()) {
                DeliveryAddressEntity newDefault = remaining.get(0);
                newDefault.setDefault(true);
                deliveryAddressRepository.save(newDefault);
            }
        }
    }

    public void setDefaultAddress(Long userId, Long addressId) {
        DeliveryAddressEntity address = deliveryAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Адрес не найден"));

        deliveryAddressRepository.clearDefaultAddresses(userId);
        address.setDefault(true);
        deliveryAddressRepository.save(address);
    }

    private DeliveryAddressDto convertToDto(DeliveryAddressEntity entity) {
        // Реализация конвертации
        return new DeliveryAddressDto(
                entity.getId(),
                entity.getUser().getId(),
                entity.getStreet(),
                entity.getHouse(),
                entity.getBuilding(),
                entity.getApartment(),
                entity.getEntrance(),
                entity.getFloor(),
                entity.getComment(),
                entity.isDefault(),
                entity.getCreatedAt()
        );
    }
}