package com.example.com.venom.service.order;

import com.example.com.venom.dto.Menu.DrinkDto;
import com.example.com.venom.dto.Menu.DrinkOptionDto;
import com.example.com.venom.dto.Menu.FoodDto;
import com.example.com.venom.dto.Menu.MenuItemDto;
import com.example.com.venom.dto.order.*;
import com.example.com.venom.entity.DeliveryAddressEntity;
import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.entity.OrderEntity;
import com.example.com.venom.entity.OrderItemEntity;
import com.example.com.venom.enums.order.OrderStatus;
import com.example.com.venom.repository.EstablishmentRepository;
import com.example.com.venom.repository.UserRepository;
import com.example.com.venom.repository.order.DeliveryAddressRepository;
import com.example.com.venom.repository.order.OrderItemRepository;
import com.example.com.venom.repository.order.OrderRepository;
import com.example.com.venom.service.BusinessException;
import com.example.com.venom.service.MenuService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final EstablishmentRepository establishmentRepository;
    private final MenuService menuService;
    private final UserRepository userRepository;
    private final OrderNotificationService notificationService;

    public OrderDto createOrder(CreateOrderRequest request, Long userId) {
        // 1. Проверяем существование заведения
        EstablishmentEntity establishment = establishmentRepository.findById(request.getEstablishmentId())
                .orElseThrow(() -> new EntityNotFoundException("Заведение не найдено"));

        // 2. Проверяем время работы заведения
        validateDeliveryTime(establishment, request.getDeliveryTime());

        // 3. Получаем адрес доставки
        DeliveryAddressEntity deliveryAddress = getDeliveryAddress(request, userId);

        // 4. Рассчитываем позиции заказа и общую стоимость
        List<OrderItemEntity> orderItems = new ArrayList<>();
        double totalPrice = 0.0;

        for (CreateOrderItemDto itemRequest : request.getItems()) {
            OrderItemEntity item = createOrderItem(itemRequest, establishment);
            orderItems.add(item);
            totalPrice += item.getTotalPrice();
        }

        // 5. Создаем заказ
        OrderEntity order = new OrderEntity();
        order.setEstablishment(establishment);
        order.setUser(userRepository.getReferenceById(userId));
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress(deliveryAddress);
        order.setContactless(request.isContactless());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setDeliveryTime(request.getDeliveryTime());
        order.setComments(request.getComments());
        order.setTotalPrice(totalPrice);

        OrderEntity savedOrder = orderRepository.save(order);

        // 6. Сохраняем позиции заказа с ссылкой на заказ
        orderItems.forEach(item -> item.setOrder(savedOrder));
        orderItemRepository.saveAll(orderItems);

        // 7. Отправляем уведомление администратору заведения
        notificationService.sendOrderCreatedNotification(savedOrder);

        return convertToDto(savedOrder, orderItems);
    }

    public List<OrderDto> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<OrderDto> getEstablishmentOrders(Long establishmentId, OrderStatus status) {
        List<OrderEntity> orders;
        if (status != null) {
            orders = orderRepository.findByEstablishmentIdAndStatusOrderByCreatedAtDesc(establishmentId, status);
        } else {
            orders = orderRepository.findByEstablishmentIdOrderByCreatedAtDesc(establishmentId);
        }

        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public OrderDto updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.getStatus());

        if (request.getStatus() == OrderStatus.REJECTED) {
            order.setRejectionReason(request.getRejectionReason());
        }

        OrderEntity updatedOrder = orderRepository.save(order);

        // Отправляем уведомление пользователю об изменении статуса
        notificationService.sendOrderStatusChangedNotification(updatedOrder, oldStatus);

        return convertToDto(updatedOrder);
    }

    public List<TimeSlotDto> getAvailableTimeSlots(Long establishmentId, LocalDate date) {
        EstablishmentEntity establishment = establishmentRepository.findById(establishmentId)
                .orElseThrow(() -> new EntityNotFoundException("Заведение не найдено"));

        List<TimeSlotDto> timeSlots = new ArrayList<>();
        LocalTime openTime = getOpeningTime(establishment, date);
        LocalTime closeTime = getClosingTime(establishment, date);

        if (openTime == null || closeTime == null) {
            return timeSlots; // Заведение не работает в этот день
        }

        LocalTime currentTime = openTime;
        LocalDateTime now = LocalDateTime.now();

        // Генерируем слоты с 30-минутным интервалом и 15-минутным шагом
        while (currentTime.isBefore(closeTime.minusMinutes(30))) {
            LocalTime endTime = currentTime.plusMinutes(30);
            LocalDateTime slotStart = LocalDateTime.of(date, currentTime);
            LocalDateTime slotEnd = LocalDateTime.of(date, endTime);

            // Проверяем, что слот в будущем (минимум через 45 минут от текущего времени)
            boolean isAvailable = slotStart.isAfter(now.plusMinutes(45)) &&
                    isSlotAvailable(establishmentId, slotStart, slotEnd);

            TimeSlotDto slot = new TimeSlotDto(
                    slotStart,
                    slotEnd,
                    currentTime.format(DateTimeFormatter.ofPattern("HH:mm")) + "-" +
                            endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    isAvailable
            );

            timeSlots.add(slot);
            currentTime = currentTime.plusMinutes(15);
        }

        return timeSlots;
    }

    private void validateDeliveryTime(EstablishmentEntity establishment, LocalDateTime deliveryTime) {
        // Проверяем, что время доставки в будущем (минимум через 45 минут)
        LocalDateTime minDeliveryTime = LocalDateTime.now().plusMinutes(45);
        if (deliveryTime.isBefore(minDeliveryTime)) {
            throw new BusinessException("Время доставки должно быть минимум через 45 минут от текущего времени");
        }

        // Проверяем время работы заведения
        LocalTime openTime = getOpeningTime(establishment, deliveryTime.toLocalDate());
        LocalTime closeTime = getClosingTime(establishment, deliveryTime.toLocalDate());

        if (openTime == null || closeTime == null ||
                deliveryTime.toLocalTime().isBefore(openTime) ||
                deliveryTime.toLocalTime().isAfter(closeTime.minusMinutes(30))) {
            throw new BusinessException("Выбранное время доставки выходит за рамки времени работы заведения");
        }
    }

    private DeliveryAddressEntity getDeliveryAddress(CreateOrderRequest request, Long userId) {
        if (request.getDeliveryAddressId() != null) {
            // Используем существующий адрес (делаем копию для истории)
            DeliveryAddressEntity existing = deliveryAddressRepository.findByIdAndUserId(
                            request.getDeliveryAddressId(), userId)
                    .orElseThrow(() -> new EntityNotFoundException("Адрес не найден"));

            DeliveryAddressEntity copy = new DeliveryAddressEntity();
            copy.setStreet(existing.getStreet());
            copy.setHouse(existing.getHouse());
            copy.setBuilding(existing.getBuilding());
            copy.setApartment(existing.getApartment());
            copy.setEntrance(existing.getEntrance());
            copy.setFloor(existing.getFloor());
            copy.setComment(existing.getComment());
            return deliveryAddressRepository.save(copy);

        } else if (request.getDeliveryAddress() != null) {
            // Создаем новый временный адрес
            DeliveryAddressEntity newAddress = new DeliveryAddressEntity();
            newAddress.setUser(userRepository.getReferenceById(userId));
            newAddress.setStreet(request.getDeliveryAddress().getStreet());
            newAddress.setHouse(request.getDeliveryAddress().getHouse());
            newAddress.setBuilding(request.getDeliveryAddress().getBuilding());
            newAddress.setApartment(request.getDeliveryAddress().getApartment());
            newAddress.setEntrance(request.getDeliveryAddress().getEntrance());
            newAddress.setFloor(request.getDeliveryAddress().getFloor());
            newAddress.setComment(request.getDeliveryAddress().getComment());
            newAddress.setDefault(false);
            return deliveryAddressRepository.save(newAddress);
        }

        return null;
    }

    private OrderItemEntity createOrderItem(CreateOrderItemDto itemRequest, EstablishmentEntity establishment) {
        // Получаем информацию о позиции меню
        Object menuItem = menuService.getMenuItemById(
                establishment.getId(),
                itemRequest.getMenuItemId(),
                itemRequest.getMenuItemType()
        );

        // Рассчитываем цену
        double pricePerUnit = calculatePrice(menuItem, itemRequest.getSelectedOptions());
        double totalPrice = pricePerUnit * itemRequest.getQuantity();

        OrderItemEntity item = new OrderItemEntity();
        MenuItemDto menuItemDto = (MenuItemDto) menuItem;
        item.setMenuItemId(menuItemDto.getId());
        item.setMenuItemName(menuItemDto.getName());
        item.setMenuItemType(itemRequest.getMenuItemType());
        item.setQuantity(itemRequest.getQuantity());
        item.setPricePerUnit(pricePerUnit);
        item.setTotalPrice(totalPrice);

        if (itemRequest.getSelectedOptions() != null) {
            item.setOptions(convertOptionsToJson(itemRequest.getSelectedOptions()));
        }

        return item;
    }

    private double calculatePrice(Object menuItem, Map<String, String> selectedOptions) {
        // Для еды - просто цена
        if (menuItem instanceof FoodDto) {
            return ((FoodDto) menuItem).getCost();
        }

        // Для напитков - цена зависит от выбранного размера
        if (menuItem instanceof DrinkDto && selectedOptions != null) {
            DrinkDto drink = (DrinkDto) menuItem;
            String sizeStr = selectedOptions.get("size");
            if (sizeStr != null) {
                int size = Integer.parseInt(sizeStr);
                return drink.getOptions().stream()
                        .filter(opt -> opt.getSizeMl() == size)
                        .map(DrinkOptionDto::getCost)
                        .findFirst()
                        .orElse(drink.getOptions().get(0).getCost());
            }
        }

        return menuItem instanceof DrinkDto ?
                ((DrinkDto) menuItem).getOptions().get(0).getCost() : 0.0;
    }

    private boolean isSlotAvailable(Long establishmentId, LocalDateTime start, LocalDateTime end) {
        // Проверяем, есть ли уже заказы на это время
        // Можно ограничить количество одновременных заказов на один временной слот
        List<OrderEntity> existingOrders = orderRepository.findOrdersByEstablishmentAndDateRange(
                establishmentId,
                start.minusMinutes(30), // За 30 минут до начала слота
                end.plusMinutes(30)     // И 30 минут после окончания
        );

        // Предположим, что заведение может обрабатывать не более 3 заказов одновременно
        return existingOrders.size() < 3;
    }

    private OrderDto convertToDto(OrderEntity order) {
        List<OrderItemEntity> orderItems = orderItemRepository.findByOrderId(order.getId());
        return convertToDto(order, orderItems);
    }

    private OrderDto convertToDto(OrderEntity order, List<OrderItemEntity> orderItems) {
        return OrderDto.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .establishmentId(order.getEstablishment().getId())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .deliveryTime(order.getDeliveryTime())
                .deliveryAddressId(order.getDeliveryAddress() != null ?
                    order.getDeliveryAddress().getId() : null)
                .items(orderItems.stream()
                    .map(this::convertOrderItemToDto)
                    .collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemDto convertOrderItemToDto(OrderItemEntity item) {
        return OrderItemDto.builder()
                .id(item.getId())
                .orderId(item.getOrder() != null ? item.getOrder().getId() : null)
                .menuItemId(item.getMenuItemId())
                .menuItemName(item.getMenuItemName())
                .menuItemType(item.getMenuItemType())
                .quantity(item.getQuantity())
                .pricePerUnit(item.getPricePerUnit())
                .totalPrice(item.getTotalPrice())
                .options(item.getOptions())
                .build();
    }

    private LocalTime getOpeningTime(EstablishmentEntity establishment, LocalDate date) {
        // Логика получения времени открытия заведения на конкретную дату
        // Пока возвращаем фиксированное время
        return LocalTime.of(9, 0); // 9:00
    }

    private LocalTime getClosingTime(EstablishmentEntity establishment, LocalDate date) {
        // Логика получения времени закрытия заведения на конкретную дату
        // Пока возвращаем фиксированное время
        return LocalTime.of(23, 0); // 23:00
    }

    private String convertOptionsToJson(Map<String, String> options) {
        // Преобразование опций в JSON строку
        if (options == null || options.isEmpty()) {
            return "{}";
        }
        // Простая реализация - можно использовать Jackson ObjectMapper
        return "{" + options.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"")
                .collect(Collectors.joining(",")) + "}";
    }
}