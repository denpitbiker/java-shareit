package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestDtoJsonTest {
    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    @DisplayName("Serialize item request dto")
    public void serialize_itemRequestDto_containsExpectedFields() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto(
                1L,
                "Need a drill",
                LocalDateTime.of(2026, 5, 11, 11, 0),
                List.of(new ItemRequestItemDto(10L, "Drill", 2L, 1L))
        );

        assertThat(json.write(requestDto)).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json.write(requestDto)).extractingJsonPathStringValue("$.description").isEqualTo("Need a drill");
        assertThat(json.write(requestDto)).extractingJsonPathStringValue("$.created").isEqualTo("2026-05-11T11:00:00");
        assertThat(json.write(requestDto)).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Drill");
    }
}
