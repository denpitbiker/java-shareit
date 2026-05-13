package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

import static ru.practicum.shareit.common.Header.USER_ID_HEADER;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private static final String LOG_CREATE_BOOKING = "Creating booking for userId={}, bookingDto={}";
    private static final String LOG_APPROVE_BOOKING = "Approving bookingId={} by userId={}, approved={}";
    private static final String LOG_GET_BOOKING_BY_ID = "Getting bookingId={} for userId={}";
    private static final String LOG_GET_USER_BOOKINGS = "Getting bookings for userId={}, state={}";
    private static final String LOG_GET_OWNER_BOOKINGS = "Getting owner bookings for userId={}, state={}";

    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                             @Valid @RequestBody BookingDto bookingDto) {
        log.info(LOG_CREATE_BOOKING, userId, bookingDto);
        return bookingService.create(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(USER_ID_HEADER) Long userId,
                              @PathVariable Long bookingId,
                              @RequestParam Boolean approved) {
        log.info(LOG_APPROVE_BOOKING, bookingId, userId, approved);
        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@RequestHeader(USER_ID_HEADER) Long userId,
                              @PathVariable Long bookingId) {
        log.info(LOG_GET_BOOKING_BY_ID, bookingId, userId);
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getUserBookings(@RequestHeader(USER_ID_HEADER) Long userId,
                                            @RequestParam(defaultValue = "ALL") String state) {
        log.info(LOG_GET_USER_BOOKINGS, userId, state);
        return bookingService.getUserBookings(userId, BookingState.from(state));
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestHeader(USER_ID_HEADER) Long userId,
                                             @RequestParam(defaultValue = "ALL") String state) {
        log.info(LOG_GET_OWNER_BOOKINGS, userId, state);
        return bookingService.getOwnerBookings(userId, BookingState.from(state));
    }
}
