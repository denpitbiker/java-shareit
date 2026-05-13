package ru.practicum.shareit.item;

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
import ru.practicum.shareit.error.ForbiddenOperationException;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

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
import static ru.practicum.shareit.TestStubs.UPDATED_ITEM_NAME;
import static ru.practicum.shareit.TestStubs.USER_ID_HEADER;
import static ru.practicum.shareit.TestStubs.VALID_COMMENT_TEXT;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_DTO_1;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_DTO_2;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_NAME_1;

@WebMvcTest(ItemController.class)
@Import(ErrorHandler.class)
public class ItemControllerTest {
    private static final String ITEMS_ROUTE = "/items";
    private static final String ITEM_ROUTE = "/items/{itemId}";
    private static final String SEARCH_ROUTE = "/items/search";
    private static final String NAME_FIELD = "$.name";
    private static final String ERROR_FIELD = "$.error";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    @DisplayName("Create valid item")
    public void post_createValidItem_success200() throws Exception {
        Mockito.when(itemService.create(1L, VALID_ITEM_DTO_1))
                .thenReturn(new ItemDto(1L, VALID_ITEM_DTO_1.getName(), VALID_ITEM_DTO_1.getDescription(), true, null));

        mvc.perform(post(ITEMS_ROUTE)
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(VALID_ITEM_DTO_1)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(NAME_FIELD).value(VALID_ITEM_NAME_1));
    }

    @Test
    @DisplayName("Update existing item by owner")
    public void patch_updateExistingItemByOwner_success200() throws Exception {
        ItemDto patchDto = new ItemDto(null, UPDATED_ITEM_NAME, null, null, null);
        Mockito.when(itemService.update(1L, 1L, patchDto))
                .thenReturn(new ItemDto(1L, UPDATED_ITEM_NAME, VALID_ITEM_DTO_1.getDescription(), true, null));

        mvc.perform(patch(ITEM_ROUTE, 1L)
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(NAME_FIELD).value(UPDATED_ITEM_NAME));
    }

    @Test
    @DisplayName("Update item by non owner")
    public void patch_updateItemByNonOwner_forbidden403() throws Exception {
        ItemDto patchDto = new ItemDto(null, UPDATED_ITEM_NAME, null, null, null);
        Mockito.when(itemService.update(2L, 1L, patchDto))
                .thenThrow(new ForbiddenOperationException("Only the owner can update the item"));

        mvc.perform(patch(ITEM_ROUTE, 1L)
                        .header(USER_ID_HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath(ERROR_FIELD).value("Only the owner can update the item"));
    }

    @Test
    @DisplayName("Get existing item")
    public void get_existingItem_success200() throws Exception {
        Mockito.when(itemService.getById(1L, null))
                .thenReturn(new ItemDto(1L, VALID_ITEM_DTO_1.getName(), VALID_ITEM_DTO_1.getDescription(), true, null));

        mvc.perform(get(ITEM_ROUTE, 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(NAME_FIELD).value(VALID_ITEM_NAME_1));
    }

    @Test
    @DisplayName("Add comment to item")
    public void post_addComment_success200() throws Exception {
        CommentDto request = new CommentDto(null, VALID_COMMENT_TEXT, null, null);
        CommentDto response = new CommentDto(1L, VALID_COMMENT_TEXT, "Lera", LocalDateTime.now());
        Mockito.when(itemService.addComment(2L, 1L, request)).thenReturn(response);

        mvc.perform(post(ITEM_ROUTE + "/comment", 1L)
                        .header(USER_ID_HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.text").value(VALID_COMMENT_TEXT))
                .andExpect(jsonPath("$.authorName").value("Lera"));
    }

    @Test
    @DisplayName("Get item with owner details")
    public void get_existingItemAsOwner_includesBookingsAndComments() throws Exception {
        ItemDto itemDto = new ItemDto(1L, VALID_ITEM_DTO_1.getName(), VALID_ITEM_DTO_1.getDescription(), true, null);
        itemDto.setComments(List.of(new CommentDto(1L, VALID_COMMENT_TEXT, "Lera", LocalDateTime.now())));
        itemDto.setLastBooking(new ru.practicum.shareit.booking.dto.BookingShortDto(10L, 2L, LocalDateTime.now().minusDays(2)));
        itemDto.setNextBooking(new ru.practicum.shareit.booking.dto.BookingShortDto(11L, 2L, LocalDateTime.now().plusDays(1)));
        Mockito.when(itemService.getById(1L, 1L)).thenReturn(itemDto);

        mvc.perform(get(ITEM_ROUTE, 1L)
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.lastBooking.id").value(10L))
                .andExpect(jsonPath("$.nextBooking.id").value(11L))
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].text").value(VALID_COMMENT_TEXT));
    }

    @Test
    @DisplayName("Get owner items")
    public void get_ownerItems_success200() throws Exception {
        Mockito.when(itemService.getOwnerItems(1L))
                .thenReturn(List.of(
                        new ItemDto(1L, VALID_ITEM_DTO_1.getName(), VALID_ITEM_DTO_1.getDescription(), true, null),
                        new ItemDto(2L, VALID_ITEM_DTO_2.getName(), VALID_ITEM_DTO_2.getDescription(), false, null)
                ));

        mvc.perform(get(ITEMS_ROUTE)
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Search available items")
    public void get_searchAvailableItems_success200() throws Exception {
        Mockito.when(itemService.search(SEARCH_TEXT_MATCH))
                .thenReturn(List.of(new ItemDto(1L, VALID_ITEM_DTO_1.getName(), VALID_ITEM_DTO_1.getDescription(), true, null)));

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
        Mockito.when(itemService.search(SEARCH_TEXT_BLANK)).thenReturn(List.of());

        mvc.perform(get(SEARCH_ROUTE)
                        .param("text", SEARCH_TEXT_BLANK))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Create item for unknown user")
    public void post_createItemForUnknownUser_notFound404() throws Exception {
        Mockito.when(itemService.create(NON_EXISTING_ID, VALID_ITEM_DTO_1))
                .thenThrow(new NotFoundException("User not found"));

        mvc.perform(post(ITEMS_ROUTE)
                        .header(USER_ID_HEADER, NON_EXISTING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(VALID_ITEM_DTO_1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(ERROR_FIELD).value("User not found"));
    }
}
