package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.ShareItApp;
import ru.practicum.shareit.error.BadRequestException;
import ru.practicum.shareit.error.ConflictException;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import static ru.practicum.shareit.TestStubs.NON_EXISTING_ID;
import static ru.practicum.shareit.TestStubs.UPDATED_EMAIL;
import static ru.practicum.shareit.TestStubs.UPDATED_USER_NAME;
import static ru.practicum.shareit.TestStubs.VALID_USER_DTO_1;
import static ru.practicum.shareit.TestStubs.VALID_USER_DTO_2;
import static ru.practicum.shareit.TestStubs.VALID_USER_NAME_1;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = ShareItApp.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserServiceImplTest {
    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Create valid user")
    public void create_validUser_userCreated() {
        UserDto createdUser = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));

        Assertions.assertNotNull(createdUser.getId(), "Expected generated id");
        Assertions.assertEquals(VALID_USER_NAME_1, createdUser.getName(), "Expected stored name");
    }

    @Test
    @DisplayName("Create user with duplicate email")
    public void create_userWithDuplicateEmail_throwsConflictException() {
        userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));

        Assertions.assertThrows(
                ConflictException.class,
                () -> userService.create(new UserDto(null, VALID_USER_DTO_2.getName(), VALID_USER_DTO_1.getEmail())),
                "Expected duplicate email conflict"
        );
    }

    @Test
    @DisplayName("Update existing user")
    public void update_existingUser_userUpdated() {
        UserDto createdUser = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));
        UserDto patch = new UserDto(null, UPDATED_USER_NAME, UPDATED_EMAIL);

        UserDto updatedUser = userService.update(createdUser.getId(), patch);

        Assertions.assertEquals(UPDATED_USER_NAME, updatedUser.getName(), "Expected updated name");
        Assertions.assertEquals(UPDATED_EMAIL, updatedUser.getEmail(), "Expected updated email");
    }

    @Test
    @DisplayName("Update unknown user")
    public void update_unknownUser_throwsNotFoundException() {
        Assertions.assertThrows(
                NotFoundException.class,
                () -> userService.update(NON_EXISTING_ID, new UserDto(null, UPDATED_USER_NAME, UPDATED_EMAIL)),
                "Expected not found for unknown user"
        );
    }

    @Test
    @DisplayName("Get existing user by id")
    public void getById_existingUser_userReturned() {
        UserDto createdUser = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));

        UserDto foundUser = userService.getById(createdUser.getId());

        Assertions.assertEquals(createdUser.getId(), foundUser.getId(), "Expected same id");
        Assertions.assertEquals(createdUser.getEmail(), foundUser.getEmail(), "Expected same email");
    }

    @Test
    @DisplayName("Get unknown user by id")
    public void getById_unknownUser_throwsNotFoundException() {
        Assertions.assertThrows(
                NotFoundException.class,
                () -> userService.getById(NON_EXISTING_ID),
                "Expected not found for unknown user"
        );
    }

    @Test
    @DisplayName("Delete existing user")
    public void delete_existingUser_userDeleted() {
        UserDto createdUser = userService.create(new UserDto(null, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));

        Assertions.assertDoesNotThrow(
                () -> userService.delete(createdUser.getId()),
                "Expected delete without exception"
        );
        Assertions.assertThrows(
                NotFoundException.class,
                () -> userService.getById(createdUser.getId()),
                "Expected deleted user to be absent"
        );
    }
}
