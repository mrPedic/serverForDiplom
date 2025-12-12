package com.example.com.venom.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.com.venom.enums.BookingStatus;
import com.example.com.venom.dto.booking.BookingCreationDto;
import com.example.com.venom.dto.booking.BookingDisplayDto;
import com.example.com.venom.entity.BookingEntity;
import com.example.com.venom.entity.EstablishmentEntity;
import com.example.com.venom.entity.TableEntity;
import com.example.com.venom.repository.BookingRepository;
import com.example.com.venom.repository.EstablishmentRepository;
import com.example.com.venom.repository.TableRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TableRepository tableRepository;
    private final EstablishmentRepository establishmentRepository;

    @Transactional
    public BookingEntity createBooking(BookingCreationDto dto) {
        TableEntity table = tableRepository.findById(dto.getTableId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Столик не найден или не принадлежит заведению."));

        if (!table.getEstablishmentId().equals(dto.getEstablishmentId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Столик не принадлежит этому заведению.");
        }

        LocalDateTime endTime = dto.getStartTime().plusMinutes(dto.getDurationMinutes());

        // Проверка пересечения с активными бронями
        List<Long> reservedIds = bookingRepository.findReservedTableIds(
                dto.getEstablishmentId(),
                dto.getStartTime(),
                endTime
        );

        if (reservedIds.contains(dto.getTableId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Столик уже забронирован на это время.");
        }

        BookingEntity booking = new BookingEntity();
        booking.setUserId(dto.getUserId());
        booking.setEstablishmentId(dto.getEstablishmentId());
        booking.setTableId(dto.getTableId());
        booking.setStartTime(dto.getStartTime());
        booking.setEndTime(endTime);
        booking.setNumPeople(dto.getNumPeople());
        booking.setNotes(dto.getNotes());
        booking.setStatus(BookingStatus.PENDING); // Важно!

        return bookingRepository.save(booking);
    }

    public List<TableEntity> getAvailableTables(Long establishmentId, LocalDateTime requestedTime) {
        LocalDateTime checkEndTime = requestedTime.plusHours(2); // или plusMinutes(dto.getDurationMinutes())

        List<Long> reservedTableIds = bookingRepository.findReservedTableIds(
                establishmentId, requestedTime, checkEndTime
        );

        return tableRepository.findByEstablishmentId(establishmentId).stream()
                .filter(t -> !reservedTableIds.contains(t.getId()))
                .collect(Collectors.toList());
    }

    public List<BookingDisplayDto> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .map(this::toDisplayDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Бронирование не найдено"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Бронирование уже отменено");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    private BookingDisplayDto toDisplayDto(BookingEntity b) {
        TableEntity table = tableRepository.findById(b.getTableId()).orElse(null);
        EstablishmentEntity est = establishmentRepository.findById(b.getEstablishmentId()).orElse(null);

        long duration = java.time.Duration.between(b.getStartTime(), b.getEndTime()).toMinutes();

        return BookingDisplayDto.builder()
                .id(b.getId())
                .establishmentName(est != null ? est.getName() : "Неизвестно")
                .establishmentAddress(est != null ? est.getAddress() : "")
                .establishmentLatitude(est != null ? est.getLatitude() : 0.0)
                .establishmentLongitude(est != null ? est.getLongitude() : 0.0)
                .tableName(table != null ? table.getName() : "Неизвестно")
                .tableMaxCapacity(table != null ? table.getMaxCapacity() : 0)
                .startTime(b.getStartTime())
                .durationMinutes(duration)
                .status(b.getStatus().name())
                .build();
    }
}