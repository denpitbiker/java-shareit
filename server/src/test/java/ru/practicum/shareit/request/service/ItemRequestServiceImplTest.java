package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static ru.practicum.shareit.TestStubs.NON_EXISTING_ID;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_DTO_1;
import static ru.practicum.shareit.TestStubs.VALID_USER_DTO_1;
import static ru.practicum.shareit.TestStubs.VALID_USER_DTO_2;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = ShareItServer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemRequestServiceImplTest {
    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Test
    @DisplayName("Create request")
    public void create_validRequest_requestCreated() {
        UserDto requestor = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));

        ItemRequestDto createdRequest = itemRequestService.create(
                requestor.getId(),
                new ItemRequestDto(null, "Need a drill", null, List.of())
        );

        Assertions.assertNotNull(createdRequest.getId(), "Expected generated request id");
        Assertions.assertEquals("Need a drill", createdRequest.getDescription(), "Expected stored description");
        Assertions.assertNotNull(createdRequest.getCreated(), "Expected creation timestamp");
    }

    @Test
    @DisplayName("Get own requests")
    public void getOwnRequests_existingRequestor_requestsReturned() {
        UserDto requestor = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        itemRequestService.create(requestor.getId(), new ItemRequestDto(null, "Need a drill", null, List.of()));

        List<ItemRequestDto> ownRequests = itemRequestService.getOwnRequests(requestor.getId());

        Assertions.assertEquals(1, ownRequests.size(), "Expected one own request");
        Assertions.assertEquals("Need a drill", ownRequests.getFirst().getDescription(), "Expected own request description");
    }

    @Test
    @DisplayName("Get all foreign requests")
    public void getAllRequests_foreignRequests_requestsReturned() {
        UserDto requestor = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        UserDto viewer = userService.create(new UserDto(null, VALID_USER_DTO_2.getName(), VALID_USER_DTO_2.getEmail()));
        itemRequestService.create(requestor.getId(), new ItemRequestDto(null, "Need a drill", null, List.of()));

        List<ItemRequestDto> allRequests = itemRequestService.getAllRequests(viewer.getId());

        Assertions.assertEquals(1, allRequests.size(), "Expected one foreign request");
        Assertions.assertEquals("Need a drill", allRequests.getFirst().getDescription(), "Expected foreign request description");
    }

    @Test
    @DisplayName("Get request by id with linked items")
    public void getById_existingRequest_requestWithItemsReturned() {
        UserDto requestor = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        UserDto owner = userService.create(new UserDto(null, VALID_USER_DTO_2.getName(), VALID_USER_DTO_2.getEmail()));
        ItemRequestDto createdRequest = itemRequestService.create(
                requestor.getId(),
                new ItemRequestDto(null, "Need a drill", null, List.of())
        );
        ItemDto itemToCreate = new ItemDto(
                null,
                VALID_ITEM_DTO_1.getName(),
                VALID_ITEM_DTO_1.getDescription(),
                VALID_ITEM_DTO_1.getAvailable(),
                createdRequest.getId()
        );
        itemService.create(owner.getId(), itemToCreate);

        ItemRequestDto foundRequest = itemRequestService.getById(requestor.getId(), createdRequest.getId());

        Assertions.assertEquals(createdRequest.getId(), foundRequest.getId(), "Expected same request id");
        Assertions.assertEquals(1, foundRequest.getItems().size(), "Expected linked item");
        Assertions.assertEquals(VALID_ITEM_DTO_1.getName(), foundRequest.getItems().getFirst().getName(), "Expected linked item name");
    }

    @Test
    @DisplayName("Get request by unknown id")
    public void getById_unknownRequest_throwsNotFoundException() {
        UserDto requestor = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));

        Assertions.assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getById(requestor.getId(), NON_EXISTING_ID),
                "Expected not found for unknown request"
        );
    }
}
