package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.ShareItApp;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.ForbiddenOperationException;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.TestStubs.NON_EXISTING_ID;
import static ru.practicum.shareit.TestStubs.VALID_COMMENT_TEXT;
import static ru.practicum.shareit.TestStubs.SEARCH_TEXT_BLANK;
import static ru.practicum.shareit.TestStubs.SEARCH_TEXT_MATCH;
import static ru.practicum.shareit.TestStubs.UPDATED_ITEM_DESCRIPTION;
import static ru.practicum.shareit.TestStubs.UPDATED_ITEM_NAME;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_DESCRIPTION_1;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_DTO_1;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_DTO_2;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_NAME_1;
import static ru.practicum.shareit.TestStubs.VALID_USER_DTO_1;
import static ru.practicum.shareit.TestStubs.VALID_USER_DTO_2;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = ShareItApp.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemServiceImplTest {
    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("Create valid item")
    public void create_validItem_itemCreated() {
        UserDto owner = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));

        ItemDto createdItem = itemService.create(owner.getId(), new ItemDto(null, VALID_ITEM_NAME_1, VALID_ITEM_DESCRIPTION_1, true, null));

        Assertions.assertNotNull(createdItem.getId(), "Expected generated id");
        Assertions.assertEquals(VALID_ITEM_NAME_1, createdItem.getName(), "Expected stored name");
    }

    @Test
    @DisplayName("Create item for unknown user")
    public void create_itemForUnknownUser_throwsNotFoundException() {
        Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.create(NON_EXISTING_ID, VALID_ITEM_DTO_1),
                "Expected not found for unknown user"
        );
    }

    @Test
    @DisplayName("Update existing item by owner")
    public void update_existingItemByOwner_itemUpdated() {
        UserDto owner = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        ItemDto createdItem = itemService.create(owner.getId(), VALID_ITEM_DTO_1);

        ItemDto updatedItem = itemService.update(
                owner.getId(),
                createdItem.getId(),
                new ItemDto(null, UPDATED_ITEM_NAME, UPDATED_ITEM_DESCRIPTION, false, null)
        );

        Assertions.assertEquals(UPDATED_ITEM_NAME, updatedItem.getName(), "Expected updated name");
        Assertions.assertEquals(UPDATED_ITEM_DESCRIPTION, updatedItem.getDescription(), "Expected updated description");
        Assertions.assertFalse(updatedItem.getAvailable(), "Expected updated availability");
    }

    @Test
    @DisplayName("Update item by non owner")
    public void update_itemByNonOwner_throwsForbiddenOperationException() {
        UserDto owner = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        UserDto anotherUser = userService.create(new UserDto(null, VALID_USER_DTO_2.getName(), VALID_USER_DTO_2.getEmail()));
        ItemDto createdItem = itemService.create(owner.getId(), VALID_ITEM_DTO_1);

        Assertions.assertThrows(
                ForbiddenOperationException.class,
                () -> itemService.update(anotherUser.getId(), createdItem.getId(), new ItemDto(null, UPDATED_ITEM_NAME, null, null, null)),
                "Expected forbidden update for non owner"
        );
    }

    @Test
    @DisplayName("Get item by id")
    public void getById_existingItem_itemReturned() {
        UserDto owner = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        ItemDto createdItem = itemService.create(owner.getId(), VALID_ITEM_DTO_1);

        ItemDto foundItem = itemService.getById(createdItem.getId());

        Assertions.assertEquals(createdItem.getId(), foundItem.getId(), "Expected same id");
        Assertions.assertEquals(createdItem.getDescription(), foundItem.getDescription(), "Expected same description");
    }

    @Test
    @DisplayName("Get owner items")
    public void getOwnerItems_existingOwner_itemsReturned() {
        UserDto owner = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        itemService.create(owner.getId(), VALID_ITEM_DTO_1);
        itemService.create(owner.getId(), VALID_ITEM_DTO_2);

        List<ItemDto> ownerItems = itemService.getOwnerItems(owner.getId());

        Assertions.assertEquals(2, ownerItems.size(), "Expected two owner items");
    }

    @Test
    @DisplayName("Search available items")
    public void search_availableItems_matchingItemsReturned() {
        UserDto owner = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        itemService.create(owner.getId(), VALID_ITEM_DTO_1);
        itemService.create(owner.getId(), VALID_ITEM_DTO_2);

        List<ItemDto> searchResult = itemService.search(SEARCH_TEXT_MATCH);

        Assertions.assertEquals(1, searchResult.size(), "Expected one matching item");
        Assertions.assertEquals(VALID_ITEM_NAME_1, searchResult.getFirst().getName(), "Expected drill to be found");
    }

    @Test
    @DisplayName("Search with blank text")
    public void search_blankText_emptyResultReturned() {
        UserDto owner = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        itemService.create(owner.getId(), VALID_ITEM_DTO_1);

        List<ItemDto> searchResult = itemService.search(SEARCH_TEXT_BLANK);

        Assertions.assertTrue(searchResult.isEmpty(), "Expected empty result for blank search");
    }

    @Test
    @DisplayName("Add comment on completed booking")
    public void addComment_completedBooking_commentReturned() {
        UserDto owner = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        UserDto booker = userService.create(new UserDto(null, VALID_USER_DTO_2.getName(), VALID_USER_DTO_2.getEmail()));
        ItemDto createdItem = itemService.create(owner.getId(), VALID_ITEM_DTO_1);
        seedCompletedBooking(booker.getId(), createdItem.getId());

        CommentDto createdComment = itemService.addComment(
                booker.getId(),
                createdItem.getId(),
                new CommentDto(null, VALID_COMMENT_TEXT, null, null)
        );

        Assertions.assertEquals(VALID_COMMENT_TEXT, createdComment.getText(), "Expected stored comment text");
        Assertions.assertEquals(1, commentRepository.findByItem_IdOrderByCreatedAsc(createdItem.getId()).size(), "Expected one stored comment");
        Assertions.assertEquals(1, itemService.getById(createdItem.getId()).getComments().size(), "Expected comment visible on item");
    }

    @Test
    @DisplayName("Get item as owner with bookings")
    public void getById_asOwner_includesBookingDetails() {
        UserDto owner = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        UserDto booker = userService.create(new UserDto(null, VALID_USER_DTO_2.getName(), VALID_USER_DTO_2.getEmail()));
        ItemDto createdItem = itemService.create(owner.getId(), VALID_ITEM_DTO_1);
        seedPastAndFutureBookings(booker.getId(), createdItem.getId());

        ItemDto itemWithDetails = itemService.getById(createdItem.getId(), owner.getId());

        Assertions.assertNotNull(itemWithDetails.getLastBooking(), "Expected last booking");
        Assertions.assertNotNull(itemWithDetails.getNextBooking(), "Expected next booking");
        Assertions.assertEquals(booker.getId(), itemWithDetails.getLastBooking().getBookerId(), "Expected last booking booker");
        Assertions.assertEquals(booker.getId(), itemWithDetails.getNextBooking().getBookerId(), "Expected next booking booker");
    }

    @Test
    @DisplayName("Get owner items with comments")
    public void getOwnerItems_includesComments() {
        UserDto owner = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        UserDto booker = userService.create(new UserDto(null, VALID_USER_DTO_2.getName(), VALID_USER_DTO_2.getEmail()));
        ItemDto createdItem = itemService.create(owner.getId(), VALID_ITEM_DTO_1);
        seedCompletedBooking(booker.getId(), createdItem.getId());
        itemService.addComment(booker.getId(), createdItem.getId(), new CommentDto(null, VALID_COMMENT_TEXT, null, null));

        List<ItemDto> ownerItems = itemService.getOwnerItems(owner.getId());

        Assertions.assertEquals(1, ownerItems.size(), "Expected one owner item");
        Assertions.assertEquals(1, ownerItems.getFirst().getComments().size(), "Expected comment to be included");
    }

    private void seedCompletedBooking(Long bookerId, Long itemId) {
        seedBooking(bookerId, itemId, LocalDateTime.now().minusDays(4), LocalDateTime.now().minusDays(2));
    }

    private void seedPastAndFutureBookings(Long bookerId, Long itemId) {
        seedBooking(bookerId, itemId, LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(3));
        seedBooking(bookerId, itemId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
    }

    private void seedBooking(Long bookerId, Long itemId, LocalDateTime start, LocalDateTime end) {
        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(itemRepository.findById(itemId).orElseThrow())
                .booker(userRepository.findById(bookerId).orElseThrow())
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(booking);
    }
}
