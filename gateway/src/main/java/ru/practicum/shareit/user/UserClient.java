package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";
    private static final String ROOT_PATH = "";
    private static final String ID_PATH_PREFIX = "/";

    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> create(UserDto userDto) {
        return post(ROOT_PATH, userDto);
    }

    public ResponseEntity<Object> update(Long userId, UserDto userDto) {
        return patch(ID_PATH_PREFIX + userId, userDto);
    }

    public ResponseEntity<Object> getById(Long userId) {
        return get(ID_PATH_PREFIX + userId);
    }

    public ResponseEntity<Object> delete(Long userId) {
        return super.delete(ID_PATH_PREFIX + userId);
    }
}
