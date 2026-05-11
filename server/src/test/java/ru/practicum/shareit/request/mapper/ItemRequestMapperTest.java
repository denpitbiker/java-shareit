package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;
import java.util.List;

public class ItemRequestMapperTest {
    @Test
    @DisplayName("Map null request to dto")
    public void toDto_nullRequest_returnsNull() {
        Assertions.assertNull(ItemRequestMapper.toDto(null, List.of()), "Expected null dto");
    }

    @Test
    @DisplayName("Map request to dto")
    public void toDto_request_fieldsPreserved() {
        LocalDateTime created = LocalDateTime.now();
        ItemRequest request = new ItemRequest(1L, "Need a drill", null, created);

        ItemRequestDto dto = ItemRequestMapper.toDto(request, List.of());

        Assertions.assertEquals(1L, dto.getId(), "Expected request id");
        Assertions.assertEquals(created, dto.getCreated(), "Expected created timestamp");
    }

    @Test
    @DisplayName("Map null item to request item dto")
    public void toItemDto_nullItem_returnsNull() {
        Assertions.assertNull(ItemRequestMapper.toItemDto(null), "Expected null dto");
    }

    @Test
    @DisplayName("Map item without owner and request to request item dto")
    public void toItemDto_itemWithoutOwnerAndRequest_idsAreNull() {
        Item item = Item.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .owner(null)
                .request(null)
                .build();

        ItemRequestItemDto dto = ItemRequestMapper.toItemDto(item);

        Assertions.assertNull(dto.getOwnerId(), "Expected null owner id");
        Assertions.assertNull(dto.getRequestId(), "Expected null request id");
        Assertions.assertEquals("Drill", dto.getName(), "Expected item name");
    }
}
