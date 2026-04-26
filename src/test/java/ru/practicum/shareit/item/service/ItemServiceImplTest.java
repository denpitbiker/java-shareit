package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.ShareItApp;
import ru.practicum.shareit.error.BadRequestException;
import ru.practicum.shareit.error.ForbiddenOperationException;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static ru.practicum.shareit.TestStubs.NON_EXISTING_ID;
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
    @DisplayName("Update item with blank description")
    public void update_itemWithBlankDescription_throwsBadRequestException() {
        UserDto owner = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        ItemDto createdItem = itemService.create(owner.getId(), VALID_ITEM_DTO_1);

        Assertions.assertThrows(
                BadRequestException.class,
                () -> itemService.update(owner.getId(), createdItem.getId(), new ItemDto(null, null, "   ", null, null)),
                "Expected bad request for blank description"
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
}
