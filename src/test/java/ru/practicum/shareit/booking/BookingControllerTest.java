package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.TestStubs.USER_ID_HEADER;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_DTO_1;
import static ru.practicum.shareit.TestStubs.VALID_USER_DTO_1;
import static ru.practicum.shareit.TestStubs.VALID_USER_DTO_2;
import static ru.practicum.shareit.TestStubs.VALID_USER_DTO_3;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = ShareItApp.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class BookingControllerTest {
    private static final String USERS_ROUTE = "/users";
    private static final String ITEMS_ROUTE = "/items";
    private static final String BOOKINGS_ROUTE = "/bookings";
    private static final String OWNER_BOOKINGS_ROUTE = "/bookings/owner";
    private static final String BOOKING_ROUTE = "/bookings/{bookingId}";
    private static final String ERROR_FIELD = "$.error";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Create booking")
    public void post_createBooking_success200() throws Exception {
        UserDto owner = extractUserDto(createUser(VALID_USER_DTO_1));
        UserDto booker = extractUserDto(createUser(VALID_USER_DTO_2));
        ItemDto item = extractItemDto(createItem(owner.getId(), VALID_ITEM_DTO_1));

        mvc.perform(post(BOOKINGS_ROUTE)
                        .header(USER_ID_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BookingDto(
                                null,
                                LocalDateTime.now().plusDays(1),
                                LocalDateTime.now().plusDays(2),
                                item.getId(),
                                null,
                                null,
                                null))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(BookingStatus.WAITING.name()))
                .andExpect(jsonPath("$.itemId").value(item.getId()));
    }

    @Test
    @DisplayName("Approve booking")
    public void patch_approveBooking_success200() throws Exception {
        UserDto owner = extractUserDto(createUser(VALID_USER_DTO_1));
        UserDto booker = extractUserDto(createUser(VALID_USER_DTO_2));
        ItemDto item = extractItemDto(createItem(owner.getId(), VALID_ITEM_DTO_1));
        BookingDto booking = extractBookingDto(createBooking(booker.getId(), item.getId()));

        mvc.perform(patch(BOOKING_ROUTE, booking.getId())
                        .header(USER_ID_HEADER, owner.getId())
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(BookingStatus.APPROVED.name()));
    }

    @Test
    @DisplayName("Get owner bookings")
    public void get_ownerBookings_success200() throws Exception {
        UserDto owner = extractUserDto(createUser(VALID_USER_DTO_1));
        UserDto booker = extractUserDto(createUser(VALID_USER_DTO_2));
        ItemDto item = extractItemDto(createItem(owner.getId(), VALID_ITEM_DTO_1));
        BookingDto booking = extractBookingDto(createBooking(booker.getId(), item.getId()));
        mvc.perform(patch(BOOKING_ROUTE, booking.getId())
                        .header(USER_ID_HEADER, owner.getId())
                        .param("approved", "true"))
                .andExpect(status().isOk());

        mvc.perform(get(OWNER_BOOKINGS_ROUTE)
                        .header(USER_ID_HEADER, owner.getId())
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(booking.getId()));
    }

    @Test
    @DisplayName("Get booking by stranger")
    public void get_bookingById_forbidden403() throws Exception {
        UserDto owner = extractUserDto(createUser(VALID_USER_DTO_1));
        UserDto booker = extractUserDto(createUser(VALID_USER_DTO_2));
        UserDto stranger = extractUserDto(createUser(VALID_USER_DTO_3));
        ItemDto item = extractItemDto(createItem(owner.getId(), VALID_ITEM_DTO_1));
        BookingDto booking = extractBookingDto(createBooking(booker.getId(), item.getId()));

        mvc.perform(get(BOOKING_ROUTE, booking.getId())
                        .header(USER_ID_HEADER, stranger.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath(ERROR_FIELD).value("Only booker or item owner can view booking"));
    }

    @Test
    @DisplayName("Get bookings with invalid state")
    public void get_userBookings_invalidState_badRequest400() throws Exception {
        UserDto owner = extractUserDto(createUser(VALID_USER_DTO_1));

        mvc.perform(get(BOOKINGS_ROUTE)
                        .header(USER_ID_HEADER, owner.getId())
                        .param("state", "wrong"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(ERROR_FIELD).value("Unknown state: wrong"));
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

    private ResultActions createBooking(Long userId, Long itemId) throws Exception {
        return mvc.perform(post(BOOKINGS_ROUTE)
                .header(USER_ID_HEADER, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new BookingDto(
                        null,
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2),
                        itemId,
                        null,
                        null,
                        null))));
    }

    private UserDto extractUserDto(ResultActions resultActions) throws Exception {
        return objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), UserDto.class);
    }

    private ItemDto extractItemDto(ResultActions resultActions) throws Exception {
        return objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), ItemDto.class);
    }

    private BookingDto extractBookingDto(ResultActions resultActions) throws Exception {
        return objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), BookingDto.class);
    }
}
