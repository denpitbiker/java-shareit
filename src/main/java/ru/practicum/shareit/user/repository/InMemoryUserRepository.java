package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.user.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@Slf4j
public class InMemoryUserRepository implements UserRepository {
    private static final String LOG_SAVE_USER = "Saving user in repository, assigned userId={}";
    private static final String LOG_UPDATE_USER = "Updating user in repository, userId={}";
    private static final String LOG_FIND_USER = "Finding user in repository by userId={}";
    private static final String LOG_FIND_USER_BY_EMAIL = "Finding user in repository by email='{}'";
    private static final String LOG_DELETE_USER = "Deleting user in repository by userId={}";

    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1L;

    @Override
    public User save(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info(LOG_SAVE_USER, user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        log.info(LOG_UPDATE_USER, user.getId());
        return user;
    }

    @Override
    public Optional<User> findById(Long userId) {
        log.info(LOG_FIND_USER, userId);
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        log.info(LOG_FIND_USER_BY_EMAIL, email);
        return users.values()
                .stream()
                .filter(user -> user.getEmail() != null && user.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public void deleteById(Long userId) {
        log.info(LOG_DELETE_USER, userId);
        users.remove(userId);
    }
}
