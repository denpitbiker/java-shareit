package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.BadRequestException;
import ru.practicum.shareit.error.ConflictException;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";
    private static final String USER_NAME_BLANK_MESSAGE = "User name must not be blank";
    private static final String USER_EMAIL_EXISTS_MESSAGE = "Email already exists";
    private static final String USER_EMAIL_INVALID_MESSAGE = "User email must be valid";
    private static final String EMAIL_SEPARATOR = "@";
    private static final String LOG_CREATE_USER = "Creating user in service, userDto={}";
    private static final String LOG_UPDATE_USER = "Updating user in service: userId={}, userDto={}";
    private static final String LOG_GET_USER = "Getting user in service by userId={}";
    private static final String LOG_DELETE_USER = "Deleting user in service by userId={}";
    private static final String LOG_EMAIL_CONFLICT = "Email uniqueness validation failed for email='{}', currentUserId={}";
    private static final String LOG_EMAIL_VALIDATION_FAILED = "Email validation failed for email='{}'";
    private static final String LOG_NAME_VALIDATION_FAILED = "User name validation failed: blank value";
    private static final String LOG_USER_NOT_FOUND = "User not found for userId={}";

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        log.info(LOG_CREATE_USER, userDto);
        validateEmailUniqueness(userDto.getEmail(), null);
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        log.info(LOG_UPDATE_USER, userId, userDto);
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
        log.info(LOG_GET_USER, userId);
        return UserMapper.toUserDto(getUserOrThrow(userId));
    }

    @Override
    public void delete(Long userId) {
        log.info(LOG_DELETE_USER, userId);
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
                    log.warn(LOG_EMAIL_CONFLICT, email, currentUserId);
                    throw new ConflictException(USER_EMAIL_EXISTS_MESSAGE);
                });
    }

    private void validateEmail(String email) {
        if (email.isBlank() || !email.contains(EMAIL_SEPARATOR)) {
            log.warn(LOG_EMAIL_VALIDATION_FAILED, email);
            throw new BadRequestException(USER_EMAIL_INVALID_MESSAGE);
        }
    }

    private void validateName(String name) {
        if (name.isBlank()) {
            log.warn(LOG_NAME_VALIDATION_FAILED);
            throw new BadRequestException(USER_NAME_BLANK_MESSAGE);
        }
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn(LOG_USER_NOT_FOUND, userId);
                    return new NotFoundException(USER_NOT_FOUND_MESSAGE);
                });
    }
}
