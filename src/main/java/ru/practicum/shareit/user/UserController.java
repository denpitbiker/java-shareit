package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private static final String LOG_CREATE_USER = "Creating user, userDto={}";
    private static final String LOG_UPDATE_USER = "Updating userId={}, userDto={}";
    private static final String LOG_GET_USER = "Getting user by userId={}";
    private static final String LOG_DELETE_USER = "Deleting user by userId={}";

    private final UserService userService;

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.info(LOG_CREATE_USER, userDto);
        return userService.create(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId, @RequestBody UserDto userDto) {
        log.info(LOG_UPDATE_USER, userId, userDto);
        return userService.update(userId, userDto);
    }

    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable Long userId) {
        log.info(LOG_GET_USER, userId);
        return userService.getById(userId);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        log.info(LOG_DELETE_USER, userId);
        userService.delete(userId);
    }
}
