package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestItemDtoJsonTest {
    @Autowired
    private JacksonTester<ItemRequestItemDto> json;

    @Test
    @DisplayName("Serialize item request item dto")
    public void serialize_itemRequestItemDto_containsExpectedFields() throws Exception {
        ItemRequestItemDto itemDto = new ItemRequestItemDto(10L, "Drill", 2L, 1L);

        assertThat(json.write(itemDto)).extractingJsonPathNumberValue("$.id").isEqualTo(10);
        assertThat(json.write(itemDto)).extractingJsonPathStringValue("$.name").isEqualTo("Drill");
        assertThat(json.write(itemDto)).extractingJsonPathNumberValue("$.ownerId").isEqualTo(2);
        assertThat(json.write(itemDto)).extractingJsonPathNumberValue("$.requestId").isEqualTo(1);
    }
}
