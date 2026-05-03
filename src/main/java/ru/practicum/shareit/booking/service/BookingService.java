package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto create(Long userId, BookingDto bookingDto);

    BookingDto approve(Long userId, Long bookingId, Boolean approved);

    BookingDto getById(Long userId, Long bookingId);

    List<BookingDto> getUserBookings(Long userId, BookingState state);

    List<BookingDto> getOwnerBookings(Long userId, BookingState state);
}
