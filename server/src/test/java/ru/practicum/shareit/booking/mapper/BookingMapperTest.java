package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static ru.practicum.shareit.TestStubs.VALID_ITEM_DESCRIPTION_1;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_NAME_1;
import static ru.practicum.shareit.TestStubs.VALID_USER_NAME_1;

public class BookingMapperTest {
    @Test
    @DisplayName("Map null booking to dto")
    public void toBookingDto_nullBooking_returnsNull() {
        Assertions.assertNull(BookingMapper.toBookingDto(null), "Expected null dto");
    }

    @Test
    @DisplayName("Map booking without item to dto")
    public void toBookingDto_bookingWithoutItem_itemIdIsNull() {
        User booker = new User(2L, VALID_USER_NAME_1, "denis@example.com");
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .item(null)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        BookingDto bookingDto = BookingMapper.toBookingDto(booking);

        Assertions.assertNull(bookingDto.getItemId(), "Expected null item id");
        Assertions.assertEquals(booker.getId(), bookingDto.getBooker().getId(), "Expected booker id");
    }

    @Test
    @DisplayName("Map booking dto without booker to short dto")
    public void toBookingShortDto_bookingWithoutBooker_bookerIdIsNull() {
        BookingDto bookingDto = new BookingDto(
                1L,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                2L,
                null,
                null,
                BookingStatus.WAITING
        );

        BookingShortDto shortDto = BookingMapper.toBookingShortDto(bookingDto);

        Assertions.assertNull(shortDto.getBookerId(), "Expected null booker id");
        Assertions.assertEquals(1L, shortDto.getId(), "Expected short dto id");
    }

    @Test
    @DisplayName("Map null booking dto to short dto")
    public void toBookingShortDto_nullBooking_returnsNull() {
        Assertions.assertNull(BookingMapper.toBookingShortDto(null), "Expected null short dto");
    }

    @Test
    @DisplayName("Map dto to booking")
    public void toBooking_dto_fieldsPreserved() {
        User user = new User(2L, VALID_USER_NAME_1, "denis@example.com");
        Item item = new Item(1L, VALID_ITEM_NAME_1, VALID_ITEM_DESCRIPTION_1, true, null, null);
        BookingDto bookingDto = new BookingDto(
                null,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                1L,
                null,
                new UserDto(user.getId(), user.getName(), user.getEmail()),
                null
        );

        Booking booking = BookingMapper.toBooking(bookingDto, user, item, BookingStatus.APPROVED);

        Assertions.assertEquals(item, booking.getItem(), "Expected item");
        Assertions.assertEquals(user, booking.getBooker(), "Expected booker");
        Assertions.assertEquals(BookingStatus.APPROVED, booking.getStatus(), "Expected status");
    }
}
