package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.practicum.shareit.ShareItApp;
import ru.practicum.shareit.user.dto.UserDto;

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

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = ShareItApp.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class UserControllerTest {
    private static final String USERS_ROUTE = "/users";
    private static final String USER_ROUTE = "/users/{userId}";
    private static final String NAME_FIELD = "$.name";
    private static final String ERROR_FIELD = "$.error";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Create valid user")
    public void post_createValidUser_success200() throws Exception {
        createUser(VALID_USER_DTO_1)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(NAME_FIELD).value(VALID_USER_NAME_1));
    }

    @Test
    @DisplayName("Create user with invalid email")
    public void post_createUserWithInvalidEmail_badRequest400() throws Exception {
        createUser(new UserDto(null, VALID_USER_DTO_1.getName(), "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Create user with duplicate email")
    public void post_createUserWithDuplicateEmail_conflict409() throws Exception {
        createUser(VALID_USER_DTO_1);

        createUser(new UserDto(null, VALID_USER_DTO_2.getName(), VALID_USER_DTO_1.getEmail()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath(ERROR_FIELD).value("Email already exists"));
    }

    @Test
    @DisplayName("Update existing user")
    public void patch_updateExistingUser_success200() throws Exception {
        UserDto createdUser = extractUserDto(createUser(VALID_USER_DTO_1));

        mvc.perform(patch(USER_ROUTE, createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserDto(null, UPDATED_USER_NAME, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(NAME_FIELD).value(UPDATED_USER_NAME));
    }

    @Test
    @DisplayName("Get existing user")
    public void get_existingUser_success200() throws Exception {
        UserDto createdUser = extractUserDto(createUser(VALID_USER_DTO_1));

        mvc.perform(get(USER_ROUTE, createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(NAME_FIELD).value(VALID_USER_NAME_1));
    }

    @Test
    @DisplayName("Get non existing user")
    public void get_nonExistingUser_notFound404() throws Exception {
        mvc.perform(get(USER_ROUTE, NON_EXISTING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(ERROR_FIELD).value("User not found"));
    }

    @Test
    @DisplayName("Delete existing user")
    public void delete_existingUser_success200() throws Exception {
        UserDto createdUser = extractUserDto(createUser(VALID_USER_DTO_1));

        mvc.perform(delete(USER_ROUTE, createdUser.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Delete non existing user")
    public void delete_nonExistingUser_notFound404() throws Exception {
        mvc.perform(delete(USER_ROUTE, NON_EXISTING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(ERROR_FIELD).value("User not found"));
    }

    private ResultActions createUser(UserDto userDto) throws Exception {
        return mvc.perform(post(USERS_ROUTE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)));
    }

    private UserDto extractUserDto(ResultActions resultActions) throws Exception {
        String content = resultActions.andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(content, UserDto.class);
    }
}
