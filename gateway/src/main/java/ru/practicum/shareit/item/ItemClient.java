package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";
    private static final String ROOT_PATH = "";
    private static final String ID_PATH_PREFIX = "/";
    private static final String SEARCH_PATH = "/search?text={text}";
    private static final String COMMENT_PATH_TEMPLATE = "/%d/comment";
    private static final String TEXT_PARAM = "text";

    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> create(Long userId, ItemDto itemDto) {
        return post(ROOT_PATH, userId, itemDto);
    }

    public ResponseEntity<Object> update(Long userId, Long itemId, ItemDto itemDto) {
        return patch(ID_PATH_PREFIX + itemId, userId, itemDto);
    }

    public ResponseEntity<Object> getById(Long userId, Long itemId) {
        return get(ID_PATH_PREFIX + itemId, userId);
    }

    public ResponseEntity<Object> getOwnerItems(Long userId) {
        return get(ROOT_PATH, userId);
    }

    public ResponseEntity<Object> search(String text) {
        return get(SEARCH_PATH, null, Map.of(TEXT_PARAM, text));
    }

    public ResponseEntity<Object> addComment(Long userId, Long itemId, CommentDto commentDto) {
        return post(COMMENT_PATH_TEMPLATE.formatted(itemId), userId, commentDto);
    }
}
