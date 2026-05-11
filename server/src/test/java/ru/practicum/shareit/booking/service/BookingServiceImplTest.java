package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.BadRequestException;
import ru.practicum.shareit.error.ForbiddenOperationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.TestStubs.VALID_COMMENT_TEXT;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_DTO_1;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_DTO_2;
import static ru.practicum.shareit.TestStubs.VALID_USER_DTO_1;
import static ru.practicum.shareit.TestStubs.VALID_USER_DTO_2;
import static ru.practicum.shareit.TestStubs.VALID_USER_DTO_3;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = ShareItServer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BookingServiceImplTest {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("Create valid booking")
    public void create_validBooking_bookingCreated() {
        UserDto owner = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        UserDto booker = userService.create(new UserDto(null, VALID_USER_DTO_2.getName(), VALID_USER_DTO_2.getEmail()));
        ItemDto item = itemService.create(owner.getId(), VALID_ITEM_DTO_1);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingDto createdBooking = bookingService.create(
                booker.getId(),
                new BookingDto(null, start, end, item.getId(), null, null, null)
        );

        Assertions.assertNotNull(createdBooking.getId(), "Expected generated booking id");
        Assertions.assertEquals(item.getId(), createdBooking.getItemId(), "Expected item id to be preserved");
        Assertions.assertEquals(booker.getId(), createdBooking.getBooker().getId(), "Expected booker id to be preserved");
        Assertions.assertEquals(BookingStatus.WAITING, createdBooking.getStatus(), "Expected waiting status");
    }

    @Test
    @DisplayName("Create booking for own item")
    public void create_bookingForOwnItem_throwsBadRequestException() {
        UserDto owner = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        ItemDto item = itemService.create(owner.getId(), VALID_ITEM_DTO_1);

        Assertions.assertThrows(
                BadRequestException.class,
                () -> bookingService.create(
                        owner.getId(),
                        new BookingDto(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item.getId(), null, null, null)
                ),
                "Expected booking to be rejected for owner"
        );
    }

    @Test
    @DisplayName("Approve booking by non owner")
    public void approve_bookingByNonOwner_throwsForbiddenOperationException() {
        UserDto owner = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        UserDto booker = userService.create(new UserDto(null, VALID_USER_DTO_2.getName(), VALID_USER_DTO_2.getEmail()));
        UserDto stranger = userService.create(new UserDto(null, VALID_USER_DTO_3.getName(), VALID_USER_DTO_3.getEmail()));
        ItemDto item = itemService.create(owner.getId(), VALID_ITEM_DTO_1);
        BookingDto createdBooking = bookingService.create(
                booker.getId(),
                new BookingDto(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item.getId(), null, null, null)
        );

        Assertions.assertThrows(
                ForbiddenOperationException.class,
                () -> bookingService.approve(stranger.getId(), createdBooking.getId(), true),
                "Expected approval to be forbidden for non owner"
        );
    }

    @Test
    @DisplayName("Get bookings by state and sort")
    public void getBookings_stateQueriesAndSorting_work() {
        UserDto owner = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        UserDto booker = userService.create(new UserDto(null, VALID_USER_DTO_2.getName(), VALID_USER_DTO_2.getEmail()));
        ItemDto item = itemService.create(owner.getId(), VALID_ITEM_DTO_1);

        BookingDto earlierBooking = bookingService.create(
                booker.getId(),
                new BookingDto(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item.getId(), null, null, null)
        );
        BookingDto laterBooking = bookingService.create(
                booker.getId(),
                new BookingDto(null, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4), item.getId(), null, null, null)
        );
        bookingService.approve(owner.getId(), earlierBooking.getId(), true);

        List<BookingDto> allBookings = bookingService.getUserBookings(booker.getId(), BookingState.ALL);
        Assertions.assertEquals(2, allBookings.size(), "Expected two bookings for booker");
        Assertions.assertEquals(laterBooking.getId(), allBookings.getFirst().getId(), "Expected newest booking first");

        List<BookingDto> waitingBookings = bookingService.getUserBookings(booker.getId(), BookingState.WAITING);
        Assertions.assertEquals(1, waitingBookings.size(), "Expected one waiting booking");
        Assertions.assertEquals(laterBooking.getId(), waitingBookings.getFirst().getId(), "Expected waiting booking to stay waiting");

        List<BookingDto> ownerBookings = bookingService.getOwnerBookings(owner.getId(), BookingState.ALL);
        Assertions.assertEquals(2, ownerBookings.size(), "Expected two bookings for owner");
        Assertions.assertEquals(laterBooking.getId(), ownerBookings.getFirst().getId(), "Expected newest owner booking first");
    }

    @Test
    @DisplayName("Get booking by stranger")
    public void getById_stranger_throwsForbiddenOperationException() {
        UserDto owner = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        UserDto booker = userService.create(new UserDto(null, VALID_USER_DTO_2.getName(), VALID_USER_DTO_2.getEmail()));
        UserDto stranger = userService.create(new UserDto(null, VALID_USER_DTO_3.getName(), VALID_USER_DTO_3.getEmail()));
        ItemDto item = itemService.create(owner.getId(), VALID_ITEM_DTO_1);
        BookingDto booking = bookingService.create(
                booker.getId(),
                new BookingDto(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item.getId(), null, null, null)
        );

        Assertions.assertThrows(
                ForbiddenOperationException.class,
                () -> bookingService.getById(stranger.getId(), booking.getId()),
                "Expected booking access to be forbidden for stranger"
        );
    }

    @Test
    @DisplayName("Add comment after completed booking")
    public void addComment_afterCompletedBooking_commentStored() {
        UserDto owner = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        UserDto booker = userService.create(new UserDto(null, VALID_USER_DTO_2.getName(), VALID_USER_DTO_2.getEmail()));
        ItemDto item = itemService.create(owner.getId(), VALID_ITEM_DTO_1);
        LocalDateTime now = LocalDateTime.now();

        User managedBooker = userRepository.findById(booker.getId()).orElseThrow();
        bookingRepository.save(Booking.builder()
                .start(now.minusDays(4))
                .end(now.minusDays(2))
                .item(itemRepository.findById(item.getId()).orElseThrow())
                .booker(managedBooker)
                .status(BookingStatus.APPROVED)
                .build());

        CommentDto createdComment = itemService.addComment(
                booker.getId(),
                item.getId(),
                new CommentDto(null, VALID_COMMENT_TEXT, null, null)
        );

        Assertions.assertNotNull(createdComment.getId(), "Expected generated comment id");
        Assertions.assertEquals(VALID_COMMENT_TEXT, createdComment.getText(), "Expected stored comment text");
        Assertions.assertEquals(managedBooker.getName(), createdComment.getAuthorName(), "Expected comment author");
        Assertions.assertEquals(1, commentRepository.findByItem_IdOrderByCreatedAsc(item.getId()).size(), "Expected persisted comment");
        Assertions.assertEquals(1, itemService.getById(item.getId()).getComments().size(), "Expected comment to be visible on item");
    }

    @Test
    @DisplayName("Add comment without completed booking")
    public void addComment_withoutCompletedBooking_throwsBadRequestException() {
        UserDto owner = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        UserDto booker = userService.create(new UserDto(null, VALID_USER_DTO_2.getName(), VALID_USER_DTO_2.getEmail()));
        ItemDto item = itemService.create(owner.getId(), VALID_ITEM_DTO_2);

        Assertions.assertThrows(
                BadRequestException.class,
                () -> itemService.addComment(booker.getId(), item.getId(), new CommentDto(null, VALID_COMMENT_TEXT, null, null)),
                "Expected comment to require completed booking"
        );
    }
}
