package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.error.BadRequestException;
import ru.practicum.shareit.error.ErrorHandler;
import ru.practicum.shareit.error.ForbiddenOperationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.TestStubs.USER_ID_HEADER;

@WebMvcTest(BookingController.class)
@Import(ErrorHandler.class)
public class BookingControllerTest {
    private static final String BOOKINGS_ROUTE = "/bookings";
    private static final String OWNER_BOOKINGS_ROUTE = "/bookings/owner";
    private static final String BOOKING_ROUTE = "/bookings/{bookingId}";
    private static final String ERROR_FIELD = "$.error";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    @DisplayName("Create booking")
    public void post_createBooking_success200() throws Exception {
        BookingDto request = new BookingDto(
                null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                1L,
                null,
                null,
                null
        );
        BookingDto response = new BookingDto(
                1L,
                request.getStart(),
                request.getEnd(),
                request.getItemId(),
                null,
                new UserDto(2L, "Lera", "lera@example.com"),
                BookingStatus.WAITING
        );
        Mockito.when(bookingService.create(2L, request)).thenReturn(response);

        mvc.perform(post(BOOKINGS_ROUTE)
                        .header(USER_ID_HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(BookingStatus.WAITING.name()))
                .andExpect(jsonPath("$.itemId").value(1L));
    }

    @Test
    @DisplayName("Approve booking")
    public void patch_approveBooking_success200() throws Exception {
        BookingDto response = new BookingDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                1L,
                null,
                new UserDto(2L, "Lera", "lera@example.com"),
                BookingStatus.APPROVED
        );
        Mockito.when(bookingService.approve(1L, 1L, true)).thenReturn(response);

        mvc.perform(patch(BOOKING_ROUTE, 1L)
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(BookingStatus.APPROVED.name()));
    }

    @Test
    @DisplayName("Get owner bookings")
    public void get_ownerBookings_success200() throws Exception {
        BookingDto booking = new BookingDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                1L,
                null,
                new UserDto(2L, "Lera", "lera@example.com"),
                BookingStatus.APPROVED
        );
        Mockito.when(bookingService.getOwnerBookings(1L, BookingState.ALL)).thenReturn(List.of(booking));

        mvc.perform(get(OWNER_BOOKINGS_ROUTE)
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("Get booking by stranger")
    public void get_bookingById_forbidden403() throws Exception {
        Mockito.when(bookingService.getById(3L, 1L))
                .thenThrow(new ForbiddenOperationException("Only booker or item owner can view booking"));

        mvc.perform(get(BOOKING_ROUTE, 1L)
                        .header(USER_ID_HEADER, 3L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath(ERROR_FIELD).value("Only booker or item owner can view booking"));
    }

    @Test
    @DisplayName("Get bookings with invalid state")
    public void get_userBookings_invalidState_badRequest400() throws Exception {
        mvc.perform(get(BOOKINGS_ROUTE)
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "wrong"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(ERROR_FIELD).value("Unknown state: wrong"));
    }

    @Test
    @DisplayName("Create booking with service validation error")
    public void post_createBooking_badRequest400() throws Exception {
        BookingDto request = new BookingDto(
                null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                1L,
                null,
                null,
                null
        );
        Mockito.when(bookingService.create(2L, request))
                .thenThrow(new BadRequestException("Item is unavailable"));

        mvc.perform(post(BOOKINGS_ROUTE)
                        .header(USER_ID_HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(ERROR_FIELD).value("Item is unavailable"));
    }
}
