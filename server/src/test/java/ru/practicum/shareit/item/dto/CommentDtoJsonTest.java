package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.practicum.shareit.TestStubs.VALID_COMMENT_TEXT;

@JsonTest
public class CommentDtoJsonTest {
    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    @DisplayName("Serialize comment dto")
    public void serialize_commentDto_containsExpectedFields() throws Exception {
        CommentDto commentDto = new CommentDto(1L, VALID_COMMENT_TEXT, "Lera", LocalDateTime.of(2026, 5, 11, 10, 0));

        assertThat(json.write(commentDto)).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json.write(commentDto)).extractingJsonPathStringValue("$.text").isEqualTo(VALID_COMMENT_TEXT);
        assertThat(json.write(commentDto)).extractingJsonPathStringValue("$.authorName").isEqualTo("Lera");
        assertThat(json.write(commentDto)).extractingJsonPathStringValue("$.created").isEqualTo("2026-05-11T10:00:00");
    }
}
