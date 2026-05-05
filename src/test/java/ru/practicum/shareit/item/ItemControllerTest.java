package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.practicum.shareit.ShareItApp;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.TestStubs.NON_EXISTING_ID;
import static ru.practicum.shareit.TestStubs.SEARCH_TEXT_BLANK;
import static ru.practicum.shareit.TestStubs.SEARCH_TEXT_MATCH;
import static ru.practicum.shareit.TestStubs.VALID_COMMENT_TEXT;
import static ru.practicum.shareit.TestStubs.UPDATED_ITEM_NAME;
import static ru.practicum.shareit.TestStubs.USER_ID_HEADER;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_DTO_1;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_DTO_2;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_NAME_1;
import static ru.practicum.shareit.TestStubs.VALID_USER_DTO_1;
import static ru.practicum.shareit.TestStubs.VALID_USER_DTO_2;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = ShareItApp.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class ItemControllerTest {
    private static final String USERS_ROUTE = "/users";
    private static final String ITEMS_ROUTE = "/items";
    private static final String ITEM_ROUTE = "/items/{itemId}";
    private static final String SEARCH_ROUTE = "/items/search";
    private static final String NAME_FIELD = "$.name";
    private static final String ERROR_FIELD = "$.error";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ru.practicum.shareit.booking.repository.BookingRepository bookingRepository;

    @Test
    @DisplayName("Create valid item")
    public void post_createValidItem_success200() throws Exception {
        UserDto owner = extractUserDto(createUser(VALID_USER_DTO_1));

        createItem(owner.getId(), VALID_ITEM_DTO_1)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(NAME_FIELD).value(VALID_ITEM_NAME_1));
    }

    @Test
    @DisplayName("Update existing item by owner")
    public void patch_updateExistingItemByOwner_success200() throws Exception {
        UserDto owner = extractUserDto(createUser(VALID_USER_DTO_1));
        ItemDto createdItem = extractItemDto(createItem(owner.getId(), VALID_ITEM_DTO_1));

        mvc.perform(patch(ITEM_ROUTE, createdItem.getId())
                        .header(USER_ID_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemDto(null, UPDATED_ITEM_NAME, null, null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(NAME_FIELD).value(UPDATED_ITEM_NAME));
    }

    @Test
    @DisplayName("Update item by non owner")
    public void patch_updateItemByNonOwner_forbidden403() throws Exception {
        UserDto owner = extractUserDto(createUser(VALID_USER_DTO_1));
        UserDto anotherUser = extractUserDto(createUser(VALID_USER_DTO_2));
        ItemDto createdItem = extractItemDto(createItem(owner.getId(), VALID_ITEM_DTO_1));

        mvc.perform(patch(ITEM_ROUTE, createdItem.getId())
                        .header(USER_ID_HEADER, anotherUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemDto(null, UPDATED_ITEM_NAME, null, null, null))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath(ERROR_FIELD).value("Only the owner can update the item"));
    }

    @Test
    @DisplayName("Get existing item")
    public void get_existingItem_success200() throws Exception {
        UserDto owner = extractUserDto(createUser(VALID_USER_DTO_1));
        ItemDto createdItem = extractItemDto(createItem(owner.getId(), VALID_ITEM_DTO_1));

        mvc.perform(get(ITEM_ROUTE, createdItem.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(NAME_FIELD).value(VALID_ITEM_NAME_1));
    }

    @Test
    @DisplayName("Add comment to item")
    public void post_addComment_success200() throws Exception {
        UserDto owner = extractUserDto(createUser(VALID_USER_DTO_1));
        UserDto booker = extractUserDto(createUser(VALID_USER_DTO_2));
        ItemDto createdItem = extractItemDto(createItem(owner.getId(), VALID_ITEM_DTO_1));
        addCompletedBooking(booker.getId(), createdItem.getId());

        mvc.perform(post(ITEM_ROUTE + "/comment", createdItem.getId())
                        .header(USER_ID_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentDto(null, VALID_COMMENT_TEXT, null, null))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.text").value(VALID_COMMENT_TEXT))
                .andExpect(jsonPath("$.authorName").value(VALID_USER_DTO_2.getName()));
    }

    @Test
    @DisplayName("Get item with owner details")
    public void get_existingItemAsOwner_includesBookingsAndComments() throws Exception {
        UserDto owner = extractUserDto(createUser(VALID_USER_DTO_1));
        UserDto booker = extractUserDto(createUser(VALID_USER_DTO_2));
        ItemDto createdItem = extractItemDto(createItem(owner.getId(), VALID_ITEM_DTO_1));
        addCompletedBooking(booker.getId(), createdItem.getId());
        addFutureBooking(booker.getId(), createdItem.getId());

        mvc.perform(post(ITEM_ROUTE + "/comment", createdItem.getId())
                        .header(USER_ID_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentDto(null, VALID_COMMENT_TEXT, null, null))))
                .andExpect(status().isOk());

        mvc.perform(get(ITEM_ROUTE, createdItem.getId())
                        .header(USER_ID_HEADER, owner.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.lastBooking.id").isNotEmpty())
                .andExpect(jsonPath("$.nextBooking.id").isNotEmpty())
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].text").value(VALID_COMMENT_TEXT));
    }

    @Test
    @DisplayName("Get owner items")
    public void get_ownerItems_success200() throws Exception {
        UserDto owner = extractUserDto(createUser(VALID_USER_DTO_1));
        createItem(owner.getId(), VALID_ITEM_DTO_1);
        createItem(owner.getId(), VALID_ITEM_DTO_2);

        mvc.perform(get(ITEMS_ROUTE)
                        .header(USER_ID_HEADER, owner.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Search available items")
    public void get_searchAvailableItems_success200() throws Exception {
        UserDto owner = extractUserDto(createUser(VALID_USER_DTO_1));
        createItem(owner.getId(), VALID_ITEM_DTO_1);
        createItem(owner.getId(), VALID_ITEM_DTO_2);

        mvc.perform(get(SEARCH_ROUTE)
                        .param("text", SEARCH_TEXT_MATCH))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value(VALID_ITEM_NAME_1));
    }

    @Test
    @DisplayName("Search with blank text")
    public void get_searchWithBlankText_success200EmptyList() throws Exception {
        UserDto owner = extractUserDto(createUser(VALID_USER_DTO_1));
        createItem(owner.getId(), VALID_ITEM_DTO_1);

        mvc.perform(get(SEARCH_ROUTE)
                        .param("text", SEARCH_TEXT_BLANK))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Create item for unknown user")
    public void post_createItemForUnknownUser_notFound404() throws Exception {
        createItem(NON_EXISTING_ID, VALID_ITEM_DTO_1)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(ERROR_FIELD).value("User not found"));
    }

    private ResultActions createUser(UserDto userDto) throws Exception {
        return mvc.perform(post(USERS_ROUTE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)));
    }

    private ResultActions createItem(Long userId, ItemDto itemDto) throws Exception {
        return mvc.perform(post(ITEMS_ROUTE)
                .header(USER_ID_HEADER, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDto)));
    }

    private UserDto extractUserDto(ResultActions resultActions) throws Exception {
        String content = resultActions.andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(content, UserDto.class);
    }

    private ItemDto extractItemDto(ResultActions resultActions) throws Exception {
        String content = resultActions.andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(content, ItemDto.class);
    }

    private void addCompletedBooking(Long bookerId, Long itemId) {
        itemRepository.findById(itemId).ifPresent(item -> bookingRepository.save(Booking.builder()
                .start(LocalDateTime.now().minusDays(4))
                .end(LocalDateTime.now().minusDays(2))
                .item(item)
                .booker(userRepository.findById(bookerId).orElseThrow())
                .status(BookingStatus.APPROVED)
                .build()));
    }

    private void addFutureBooking(Long bookerId, Long itemId) {
        itemRepository.findById(itemId).ifPresent(item -> bookingRepository.save(Booking.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(userRepository.findById(bookerId).orElseThrow())
                .status(BookingStatus.APPROVED)
                .build()));
    }

}
