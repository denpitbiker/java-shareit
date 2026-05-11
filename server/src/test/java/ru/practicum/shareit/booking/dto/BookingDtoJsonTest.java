package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoJsonTest {
    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    @DisplayName("Serialize booking dto")
    public void serialize_bookingDto_containsExpectedFields() throws Exception {
        BookingDto bookingDto = new BookingDto(
                1L,
                LocalDateTime.of(2026, 5, 12, 10, 0),
                LocalDateTime.of(2026, 5, 13, 10, 0),
                3L,
                null,
                new UserDto(2L, "Lera", "lera@example.com"),
                BookingStatus.APPROVED
        );

        assertThat(json.write(bookingDto)).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json.write(bookingDto)).extractingJsonPathStringValue("$.start").isEqualTo("2026-05-12T10:00:00");
        assertThat(json.write(bookingDto)).extractingJsonPathNumberValue("$.itemId").isEqualTo(3);
        assertThat(json.write(bookingDto)).extractingJsonPathNumberValue("$.booker.id").isEqualTo(2);
        assertThat(json.write(bookingDto)).extractingJsonPathStringValue("$.status").isEqualTo(BookingStatus.APPROVED.name());
    }
}
