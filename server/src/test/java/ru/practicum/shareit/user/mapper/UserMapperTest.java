package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static ru.practicum.shareit.TestStubs.VALID_EMAIL_1;
import static ru.practicum.shareit.TestStubs.VALID_USER_NAME_1;

public class UserMapperTest {
    @Test
    @DisplayName("Map null user to dto")
    public void toUserDto_nullUser_returnsNull() {
        Assertions.assertNull(UserMapper.toUserDto(null), "Expected null dto");
    }

    @Test
    @DisplayName("Map null dto to user")
    public void toUser_nullDto_returnsNull() {
        Assertions.assertNull(UserMapper.toUser(null), "Expected null user");
    }

    @Test
    @DisplayName("Map user to dto and back")
    public void map_userAndDto_fieldsPreserved() {
        User user = new User(1L, VALID_USER_NAME_1, VALID_EMAIL_1);

        UserDto userDto = UserMapper.toUserDto(user);
        User mappedUser = UserMapper.toUser(userDto);

        Assertions.assertEquals(user.getId(), userDto.getId(), "Expected dto id");
        Assertions.assertEquals(user.getName(), mappedUser.getName(), "Expected user name");
        Assertions.assertEquals(user.getEmail(), mappedUser.getEmail(), "Expected user email");
    }
}
