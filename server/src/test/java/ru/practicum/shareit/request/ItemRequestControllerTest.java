package ru.practicum.shareit.request;

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
import ru.practicum.shareit.error.ErrorHandler;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.TestStubs.USER_ID_HEADER;

@WebMvcTest(ItemRequestController.class)
@Import(ErrorHandler.class)
public class ItemRequestControllerTest {
    private static final String REQUESTS_ROUTE = "/requests";
    private static final String REQUEST_ROUTE = "/requests/{requestId}";
    private static final String ALL_REQUESTS_ROUTE = "/requests/all";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    @DisplayName("Create request")
    public void post_createRequest_success200() throws Exception {
        ItemRequestDto request = new ItemRequestDto(null, "Need a drill", null, List.of());
        ItemRequestDto response = new ItemRequestDto(1L, "Need a drill", LocalDateTime.now(), List.of());
        Mockito.when(itemRequestService.create(1L, request)).thenReturn(response);

        mvc.perform(post(REQUESTS_ROUTE)
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.description").value("Need a drill"));
    }

    @Test
    @DisplayName("Get own requests")
    public void get_ownRequests_success200() throws Exception {
        ItemRequestDto response = new ItemRequestDto(1L, "Need a drill", LocalDateTime.now(), List.of());
        Mockito.when(itemRequestService.getOwnRequests(1L)).thenReturn(List.of(response));

        mvc.perform(get(REQUESTS_ROUTE)
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("Get all requests")
    public void get_allRequests_success200() throws Exception {
        ItemRequestDto response = new ItemRequestDto(2L, "Need a saw", LocalDateTime.now(), List.of());
        Mockito.when(itemRequestService.getAllRequests(1L)).thenReturn(List.of(response));

        mvc.perform(get(ALL_REQUESTS_ROUTE)
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description").value("Need a saw"));
    }

    @Test
    @DisplayName("Get request by id")
    public void get_requestById_success200() throws Exception {
        ItemRequestDto response = new ItemRequestDto(
                1L,
                "Need a drill",
                LocalDateTime.now(),
                List.of(new ItemRequestItemDto(10L, "Drill", 2L, 1L))
        );
        Mockito.when(itemRequestService.getById(1L, 1L)).thenReturn(response);

        mvc.perform(get(REQUEST_ROUTE, 1L)
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].name").value("Drill"));
    }

    @Test
    @DisplayName("Get request by unknown id")
    public void get_requestByUnknownId_notFound404() throws Exception {
        Mockito.when(itemRequestService.getById(1L, 99L))
                .thenThrow(new NotFoundException("Item request not found"));

        mvc.perform(get(REQUEST_ROUTE, 99L)
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Item request not found"));
    }
}
