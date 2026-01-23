package com.example.com.venom.service.order;

import com.example.com.venom.dto.Menu.MenuItemDto;
import com.example.com.venom.dto.order.*;
import com.example.com.venom.entity.*;
import com.example.com.venom.enums.order.MenuItemType;
import com.example.com.venom.enums.order.OrderStatus;
import com.example.com.venom.repository.EstablishmentRepository;
import com.example.com.venom.repository.UserRepository;
import com.example.com.venom.repository.order.OrderItemRepository;
import com.example.com.venom.repository.order.OrderRepository;
import com.example.com.venom.service.MenuService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.DayOfWeek;
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
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final EstablishmentRepository establishmentRepository;
    private final OrderNotificationService notificationService;
    private final OrderPriceCalculator priceCalculator;
    private final MenuService menuService;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Для парсинга JSON расписания

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request, Long userId) {
        // 1. ВАЛИДАЦИЯ ДАТЫ: Проверяем, что заказ не слишком далеко в будущем
        if (request.getDeliveryTime() != null) {
            LocalDateTime maxDate = LocalDateTime.now().plusWeeks(2);
            if (request.getDeliveryTime().isAfter(maxDate)) {
                throw new IllegalArgumentException("Заказ можно оформить максимум на 2 недели вперед");
            }
            if (request.getDeliveryTime().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Время доставки не может быть в прошлом");
            }
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        EstablishmentEntity establishment = establishmentRepository.findById(request.getEstablishmentId())
                .orElseThrow(() -> new EntityNotFoundException("Заведение не найдено"));

        OrderEntity order = new OrderEntity();
        order.setUserId(user.getId());
        order.setEstablishmentId(establishment.getId());
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddressId(request.getDeliveryAddressId());
        order.setContactless(request.isContactless());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setDeliveryTime(request.getDeliveryTime());
        order.setComments(request.getComments());

        // --- ИСПРАВЛЕНИЕ ЦЕНЫ ---
        // Вместо priceCalculator.calculateTotalPrice (который добавляет 200р), считаем сумму позиций вручную
        double calculatedTotal = 0.0;

        // Сначала сохраняем заказ, чтобы получить ID
        // (Цену обновим после подсчета всех позиций)
        order.setTotalPrice(0.0);
        OrderEntity savedOrder = orderRepository.save(order);

        List<OrderItemEntity> items = new ArrayList<>();

        for (CreateOrderItemDto itemDto : request.getItems()) {
            OrderItemEntity item = new OrderItemEntity();
            item.setOrderId(savedOrder.getId());  // Устанавливаем orderId (Long)

            item.setMenuItemId(itemDto.getMenuItemId());
            item.setMenuItemType(itemDto.getMenuItemType());

            // Получаем информацию о меню
            MenuItemDto menuItemInfo = getMenuItemInfo(
                    itemDto.getMenuItemId(),
                    itemDto.getMenuItemType(),
                    establishment.getId()
            );

            item.setMenuItemName(menuItemInfo.getName());

            item.setQuantity(itemDto.getQuantity());

            // Рассчитываем цену позиции
            double itemPrice = calculateItemPrice(itemDto, establishment);
            item.setPricePerUnit(itemPrice);
            double itemTotal = itemPrice * itemDto.getQuantity();
            item.setTotalPrice(itemTotal);

            calculatedTotal += itemTotal;

            // Опции (для напитков) — напрямую Map → Map (Hibernate сам сериализует)
            if (itemDto.getSelectedOptions() != null) {
                item.setOptions(itemDto.getSelectedOptions());
            }

            OrderItemEntity savedItem = orderItemRepository.save(item);
            items.add(savedItem);
        }

        // Обновляем общую цену заказа
        savedOrder.setTotalPrice(calculatedTotal);
        orderRepository.save(savedOrder);

        // Отправляем уведомление заведению (используем существующий метод)
        notificationService.sendOrderCreatedNotification(savedOrder);

        return convertToDto(savedOrder);
    }

    public OrderDto getOrderById(Long id) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));
        return convertToDto(entity);
    }

    public List<OrderDto> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TimeSlotDto> getAvailableTimeSlots(Long establishmentId, LocalDate date) {
        EstablishmentEntity establishment = establishmentRepository.findById(establishmentId)
                .orElseThrow(() -> new EntityNotFoundException("Заведение не найдено"));

        // Получаем расписание (теперь строка)
        String scheduleJson = establishment.getOperatingHoursString();

        Map<String, String> schedule;
        try {
            schedule = objectMapper.readValue(scheduleJson, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка парсинга расписания заведения");
        }

        // Получаем день недели на русском
        String dayKey = getRussianDayKey(date.getDayOfWeek());

        String hours = schedule.getOrDefault(dayKey, "Закрыто");

        if (hours.equals("Закрыто")) {
            return new ArrayList<>(); // Нет слотов
        }

        // Парсим время открытия/закрытия
        String[] parts = hours.split(" - ");
        if (parts.length != 2) {
            throw new IllegalStateException("Неверный формат часов работы");
        }

        LocalTime openTime = LocalTime.parse(parts[0]);
        LocalTime closeTime = LocalTime.parse(parts[1]);

        // Генерируем слоты
        return generateSlots(date, openTime, closeTime);
    }

    private List<TimeSlotDto> generateSlots(LocalDate date, LocalTime open, LocalTime close) {
        List<TimeSlotDto> slots = new ArrayList<>();
        LocalTime current = open;

        // Если дата сегодня, начинаем слоты не раньше чем сейчас + 45 мин на готовку
        if (date.isEqual(LocalDate.now())) {
            LocalTime nowPlusPrep = LocalTime.now().plusMinutes(45);
            // Округляем до ближайших 15 минут в большую сторону
            int minute = nowPlusPrep.getMinute();
            int remainder = minute % 15;
            if (remainder != 0) {
                nowPlusPrep = nowPlusPrep.plusMinutes(15 - remainder);
            }

            if (nowPlusPrep.isAfter(current)) {
                current = nowPlusPrep;
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        // Генерируем слоты
        while (current.isBefore(close.minusMinutes(30)) || current.equals(close.minusMinutes(30))) {
            LocalTime slotEndTime = current.plusMinutes(30);

            if (slotEndTime.isAfter(close)) break;

            // Создаем LocalDateTime для начала и конца слота
            LocalDateTime startDateTime = LocalDateTime.of(date, current);
            LocalDateTime endDateTime = LocalDateTime.of(date, slotEndTime);

            // Формируем текст для отображения (например, "12:30 - 13:00")
            String label = current.format(formatter) + " - " + slotEndTime.format(formatter);

            slots.add(new TimeSlotDto(startDateTime, endDateTime, label, true));

            // Шаг генерации — 15 минут
            current = current.plusMinutes(15);
        }

        return slots;
    }

    private String getRussianDayKey(DayOfWeek day) {
        switch (day) {
            case MONDAY: return "Пн";
            case TUESDAY: return "Вт";
            case WEDNESDAY: return "Ср";
            case THURSDAY: return "Чт";
            case FRIDAY: return "Пт";
            case SATURDAY: return "Сб";
            case SUNDAY: return "Вс";
            default: return "Пн";
        }
    }

    private OrderDto convertToDto(OrderEntity entity) {
        // Инициализируем коллекцию items (на всякий случай, хотя в unidirectional OneToMany она обычно загружается)
        Hibernate.initialize(entity.getItems());

        List<OrderItemDto> itemsDto = entity.getItems().stream()
                .map(item -> OrderItemDto.builder()
                        .id(item.getId())
                        .orderId(item.getOrderId())
                        .menuItemId(item.getMenuItemId())
                        .menuItemName(item.getMenuItemName())
                        .menuItemType(item.getMenuItemType())
                        .quantity(item.getQuantity())
                        .pricePerUnit(item.getPricePerUnit())
                        .totalPrice(item.getTotalPrice())
                        .options(item.getOptions())  // Прямо Map<String, String> → Map (без сериализации в String)
                        .build())
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .establishmentId(entity.getEstablishmentId())
                .status(entity.getStatus())
                .totalPrice(entity.getTotalPrice())
                .deliveryTime(entity.getDeliveryTime())
                .deliveryAddressId(entity.getDeliveryAddressId())
                .isContactless(entity.isContactless())
                .paymentMethod(entity.getPaymentMethod())
                .comments(entity.getComments())
                .rejectionReason(entity.getRejectionReason())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .items(itemsDto)
                .build();
    }

    private MenuItemDto getMenuItemInfo(Long menuItemId, MenuItemType type, Long establishmentId) {
        Object menuItem = menuService.getMenuItemById(establishmentId, menuItemId, type);
        if (menuItem instanceof MenuItemDto) {
            return (MenuItemDto) menuItem;
        }
        throw new IllegalArgumentException("Invalid menu item type or not found");
    }

    private double calculateItemPrice(CreateOrderItemDto itemDto, EstablishmentEntity establishment) {
        return priceCalculator.calculateItemPrice(itemDto, establishment);
    }
}