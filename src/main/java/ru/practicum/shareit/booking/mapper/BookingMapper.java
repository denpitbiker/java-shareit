package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.mapper.UserMapper;

public class BookingMapper {
    private BookingMapper() {
    }

    public static BookingDto toBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        Long itemId = booking.getItem() != null ? booking.getItem().getId() : null;
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                itemId,
                ItemMapper.toItemDto(booking.getItem()),
                UserMapper.toUserDto(booking.getBooker()),
                booking.getStatus()
        );
    }

    public static BookingShortDto toBookingShortDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        Long bookerId = booking.getBooker() != null ? booking.getBooker().getId() : null;
        return new BookingShortDto(booking.getId(), bookerId);
    }
}
