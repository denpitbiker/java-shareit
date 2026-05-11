package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.practicum.shareit.TestStubs.VALID_COMMENT_TEXT;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_DESCRIPTION_1;
import static ru.practicum.shareit.TestStubs.VALID_ITEM_NAME_1;

@JsonTest
public class ItemDtoJsonTest {
    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    @DisplayName("Serialize item dto")
    public void serialize_itemDto_containsExpectedFields() throws Exception {
        ItemDto itemDto = new ItemDto(1L, VALID_ITEM_NAME_1, VALID_ITEM_DESCRIPTION_1, true, 3L);
        itemDto.setLastBooking(new BookingShortDto(10L, 2L, LocalDateTime.of(2026, 5, 10, 12, 0)));
        itemDto.setNextBooking(new BookingShortDto(11L, 2L, LocalDateTime.of(2026, 5, 12, 12, 0)));
        itemDto.setComments(List.of(new CommentDto(7L, VALID_COMMENT_TEXT, "Lera", LocalDateTime.of(2026, 5, 11, 9, 30))));

        assertThat(json.write(itemDto)).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json.write(itemDto)).extractingJsonPathStringValue("$.name").isEqualTo(VALID_ITEM_NAME_1);
        assertThat(json.write(itemDto)).extractingJsonPathNumberValue("$.requestId").isEqualTo(3);
        assertThat(json.write(itemDto)).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(10);
        assertThat(json.write(itemDto)).extractingJsonPathStringValue("$.comments[0].text").isEqualTo(VALID_COMMENT_TEXT);
    }
}
