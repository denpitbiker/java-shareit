package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

public interface UserService {
    UserDto create(UserDto userDto);

    UserDto update(Long userId, UserDto userDto);

    UserDto getById(Long userId);

    void delete(Long userId);
}
