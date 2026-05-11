package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.error.ConflictException;
import ru.practicum.shareit.error.ErrorHandler;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.TestStubs.NON_EXISTING_ID;
import static ru.practicum.shareit.TestStubs.UPDATED_USER_NAME;
import static ru.practicum.shareit.TestStubs.VALID_USER_DTO_1;
import static ru.practicum.shareit.TestStubs.VALID_USER_DTO_2;
import static ru.practicum.shareit.TestStubs.VALID_USER_NAME_1;

@WebMvcTest(UserController.class)
@Import(ErrorHandler.class)
public class UserControllerTest {
    private static final String USERS_ROUTE = "/users";
    private static final String USER_ROUTE = "/users/{userId}";
    private static final String NAME_FIELD = "$.name";
    private static final String ERROR_FIELD = "$.error";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("Create valid user")
    public void post_createValidUser_success200() throws Exception {
        Mockito.when(userService.create(VALID_USER_DTO_1))
                .thenReturn(new UserDto(1L, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));

        mvc.perform(post(USERS_ROUTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(VALID_USER_DTO_1)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(NAME_FIELD).value(VALID_USER_NAME_1));
    }

    @Test
    @DisplayName("Create user with invalid email")
    public void post_createUserWithInvalidEmail_badRequest400() throws Exception {
        mvc.perform(post(USERS_ROUTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserDto(null, VALID_USER_DTO_1.getName(), "invalid"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("User email must be valid"));
    }

    @Test
    @DisplayName("Create user with duplicate email")
    public void post_createUserWithDuplicateEmail_conflict409() throws Exception {
        Mockito.when(userService.create(VALID_USER_DTO_2))
                .thenThrow(new ConflictException("Email already exists"));

        mvc.perform(post(USERS_ROUTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(VALID_USER_DTO_2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath(ERROR_FIELD).value("Email already exists"));
    }

    @Test
    @DisplayName("Update existing user")
    public void patch_updateExistingUser_success200() throws Exception {
        Mockito.when(userService.update(1L, new UserDto(null, UPDATED_USER_NAME, null)))
                .thenReturn(new UserDto(1L, UPDATED_USER_NAME, VALID_USER_DTO_1.getEmail()));

        mvc.perform(patch(USER_ROUTE, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserDto(null, UPDATED_USER_NAME, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(NAME_FIELD).value(UPDATED_USER_NAME));
    }

    @Test
    @DisplayName("Get existing user")
    public void get_existingUser_success200() throws Exception {
        Mockito.when(userService.getById(1L))
                .thenReturn(new UserDto(1L, VALID_USER_DTO_1.getName(), VALID_USER_DTO_1.getEmail()));

        mvc.perform(get(USER_ROUTE, 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(NAME_FIELD).value(VALID_USER_NAME_1));
    }

    @Test
    @DisplayName("Get non existing user")
    public void get_nonExistingUser_notFound404() throws Exception {
        Mockito.when(userService.getById(NON_EXISTING_ID))
                .thenThrow(new NotFoundException("User not found"));

        mvc.perform(get(USER_ROUTE, NON_EXISTING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(ERROR_FIELD).value("User not found"));
    }

    @Test
    @DisplayName("Delete existing user")
    public void delete_existingUser_success200() throws Exception {
        mvc.perform(delete(USER_ROUTE, 1L))
                .andExpect(status().isOk());

        Mockito.verify(userService).delete(1L);
    }

    @Test
    @DisplayName("Delete non existing user")
    public void delete_nonExistingUser_notFound404() throws Exception {
        Mockito.doThrow(new NotFoundException("User not found"))
                .when(userService).delete(NON_EXISTING_ID);

        mvc.perform(delete(USER_ROUTE, NON_EXISTING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(ERROR_FIELD).value("User not found"));
    }
}
