package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.BadRequestException;
import ru.practicum.shareit.error.ForbiddenOperationException;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {
    private static final String LOG_CREATE_BOOKING = "Creating booking for userId={}, bookingDto={}";
    private static final String LOG_APPROVE_BOOKING = "Changing approval for bookingId={} by userId={}, approved={}";
    private static final String LOG_GET_BOOKING_BY_ID = "Getting bookingId={} for userId={}";
    private static final String LOG_GET_USER_BOOKINGS = "Getting bookings for userId={}, state={}";
    private static final String LOG_GET_OWNER_BOOKINGS = "Getting owner bookings for userId={}, state={}";
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";
    private static final String ITEM_NOT_FOUND_MESSAGE = "Item not found";
    private static final String BOOKING_NOT_FOUND_MESSAGE = "Booking not found";
    private static final String ITEM_UNAVAILABLE_MESSAGE = "Item is unavailable";
    private static final String OWNER_BOOKING_MESSAGE = "Item owner cannot book own item";
    private static final String OWNER_APPROVAL_ONLY_MESSAGE = "Only item owner can approve booking";
    private static final String BOOKING_ACCESS_DENIED_MESSAGE = "Only booker or item owner can view booking";
    private static final String BOOKING_STATUS_FINAL_MESSAGE = "Booking status can be changed only while waiting";
    private static final Sort START_DESC = Sort.by(Sort.Direction.DESC, "start");

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingDto create(Long userId, BookingDto bookingDto) {
        log.info(LOG_CREATE_BOOKING, userId, bookingDto);
        User booker = getUserOrThrowNotFound(userId);
        Item item = getItemOrThrow(bookingDto.getItemId());

        if (item.getOwner().getId().equals(userId)) {
            throw new BadRequestException(OWNER_BOOKING_MESSAGE);
        }
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new BadRequestException(ITEM_UNAVAILABLE_MESSAGE);
        }

        Booking booking = BookingMapper.toBooking(bookingDto, booker, item, BookingStatus.WAITING);

        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto approve(Long userId, Long bookingId, Boolean approved) {
        log.info(LOG_APPROVE_BOOKING, bookingId, userId, approved);
        getUserOrThrowBadRequest(userId);
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenOperationException(OWNER_APPROVAL_ONLY_MESSAGE);
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException(BOOKING_STATUS_FINAL_MESSAGE);
        }

        booking.setStatus(Boolean.TRUE.equals(approved) ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getById(Long userId, Long bookingId) {
        log.info(LOG_GET_BOOKING_BY_ID, bookingId, userId);
        getUserOrThrowNotFound(userId);
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenOperationException(BOOKING_ACCESS_DENIED_MESSAGE);
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getUserBookings(Long userId, BookingState state) {
        log.info(LOG_GET_USER_BOOKINGS, userId, state);
        getUserOrThrowNotFound(userId);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByBooker_Id(userId, START_DESC);
            case CURRENT -> bookingRepository.findByBooker_IdAndStartBeforeAndEndAfter(userId, now, now, START_DESC);
            case PAST -> bookingRepository.findByBooker_IdAndEndBefore(userId, now, START_DESC);
            case FUTURE -> bookingRepository.findByBooker_IdAndStartAfter(userId, now, START_DESC);
            case WAITING -> bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.WAITING, START_DESC);
            case REJECTED -> bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.REJECTED, START_DESC);
        };

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getOwnerBookings(Long userId, BookingState state) {
        log.info(LOG_GET_OWNER_BOOKINGS, userId, state);
        getUserOrThrowNotFound(userId);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByItem_Owner_Id(userId, START_DESC);
            case CURRENT -> bookingRepository.findByItem_Owner_IdAndStartBeforeAndEndAfter(userId, now, now, START_DESC);
            case PAST -> bookingRepository.findByItem_Owner_IdAndEndBefore(userId, now, START_DESC);
            case FUTURE -> bookingRepository.findByItem_Owner_IdAndStartAfter(userId, now, START_DESC);
            case WAITING -> bookingRepository.findByItem_Owner_IdAndStatus(userId, BookingStatus.WAITING, START_DESC);
            case REJECTED -> bookingRepository.findByItem_Owner_IdAndStatus(userId, BookingStatus.REJECTED, START_DESC);
        };

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    private User getUserOrThrowBadRequest(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(USER_NOT_FOUND_MESSAGE));
    }

    private User getUserOrThrowNotFound(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MESSAGE));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND_MESSAGE));
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(BOOKING_NOT_FOUND_MESSAGE));
    }
}
