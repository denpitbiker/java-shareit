package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";
    private static final String ROOT_PATH = "";
    private static final String ALL_REQUESTS_PATH = "/all";
    private static final String ID_PATH_PREFIX = "/";

    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> create(Long userId, ItemRequestDto requestDto) {
        return post(ROOT_PATH, userId, requestDto);
    }

    public ResponseEntity<Object> getOwnRequests(Long userId) {
        return get(ROOT_PATH, userId);
    }

    public ResponseEntity<Object> getAllRequests(Long userId) {
        return get(ALL_REQUESTS_PATH, userId);
    }

    public ResponseEntity<Object> getById(Long userId, Long requestId) {
        return get(ID_PATH_PREFIX + requestId, userId);
    }
}
