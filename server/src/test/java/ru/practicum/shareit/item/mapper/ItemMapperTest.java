package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;

import static ru.practicum.shareit.TestStubs.VALID_ITEM_DESCRIPTION_1;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_NAME_1;

public class ItemMapperTest {
    @Test
    @DisplayName("Map null item to dto")
    public void toItemDto_nullItem_returnsNull() {
        Assertions.assertNull(ItemMapper.toItemDto(null), "Expected null dto");
    }

    @Test
    @DisplayName("Map item with request to dto")
    public void toItemDto_itemWithRequest_requestIdPreserved() {
        ItemRequest request = new ItemRequest(3L, "Need item", null, LocalDateTime.now());
        Item item = Item.builder()
                .id(1L)
                .name(VALID_ITEM_NAME_1)
                .description(VALID_ITEM_DESCRIPTION_1)
                .available(true)
                .request(request)
                .build();

        ItemDto itemDto = ItemMapper.toItemDto(item);

        Assertions.assertEquals(3L, itemDto.getRequestId(), "Expected request id");
        Assertions.assertEquals(VALID_ITEM_NAME_1, itemDto.getName(), "Expected item name");
    }

    @Test
    @DisplayName("Map null dto to item")
    public void toItem_nullDto_returnsNull() {
        Assertions.assertNull(ItemMapper.toItem(null), "Expected null item");
    }
}
