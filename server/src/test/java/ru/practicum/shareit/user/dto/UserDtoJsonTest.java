package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.practicum.shareit.TestStubs.VALID_EMAIL_1;
import static ru.practicum.shareit.TestStubs.VALID_USER_NAME_1;

@JsonTest
public class UserDtoJsonTest {
    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    @DisplayName("Serialize user dto")
    public void serialize_userDto_containsExpectedFields() throws Exception {
        UserDto userDto = new UserDto(1L, VALID_USER_NAME_1, VALID_EMAIL_1);

        assertThat(json.write(userDto)).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json.write(userDto)).extractingJsonPathStringValue("$.name").isEqualTo(VALID_USER_NAME_1);
        assertThat(json.write(userDto)).extractingJsonPathStringValue("$.email").isEqualTo(VALID_EMAIL_1);
    }
}
