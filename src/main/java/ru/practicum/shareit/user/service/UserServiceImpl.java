package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.BadRequestException;
import ru.practicum.shareit.error.ConflictException;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";
    private static final String USER_NAME_BLANK_MESSAGE = "User name must not be blank";
    private static final String USER_EMAIL_EXISTS_MESSAGE = "Email already exists";
    private static final String USER_EMAIL_INVALID_MESSAGE = "User email must be valid";

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        validateEmailUniqueness(userDto.getEmail(), null);
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User existingUser = getUserOrThrow(userId);

        if (userDto.getName() != null) {
            validateName(userDto.getName());
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            validateEmail(userDto.getEmail());
            validateEmailUniqueness(userDto.getEmail(), userId);
            existingUser.setEmail(userDto.getEmail());
        }

        return UserMapper.toUserDto(userRepository.update(existingUser));
    }

    @Override
    public UserDto getById(Long userId) {
        return UserMapper.toUserDto(getUserOrThrow(userId));
    }

    @Override
    public void delete(Long userId) {
        getUserOrThrow(userId);
        userRepository.deleteById(userId);
    }

    private void validateEmailUniqueness(String email, Long currentUserId) {
        if (email == null) {
            return;
        }
        userRepository.findByEmail(email)
                .filter(user -> !user.getId().equals(currentUserId))
                .ifPresent(user -> {
                    throw new ConflictException(USER_EMAIL_EXISTS_MESSAGE);
                });
    }

    private void validateEmail(String email) {
        if (email.isBlank() || !email.contains("@")) {
            throw new BadRequestException(USER_EMAIL_INVALID_MESSAGE);
        }
    }

    private void validateName(String name) {
        if (name.isBlank()) {
            throw new BadRequestException(USER_NAME_BLANK_MESSAGE);
        }
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MESSAGE));
    }
}
