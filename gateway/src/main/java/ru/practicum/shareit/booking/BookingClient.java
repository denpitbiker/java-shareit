package ru.practicum.shareit.booking;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.BaseClient;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";
    private static final String ROOT_PATH = "";
    private static final String ID_PATH_PREFIX = "/";
    private static final String APPROVE_PATH_TEMPLATE = "/%d?approved={approved}";
    private static final String USER_BOOKINGS_PATH = "?state={state}";
    private static final String OWNER_BOOKINGS_PATH = "/owner?state={state}";
    private static final String APPROVED_PARAM = "approved";
    private static final String STATE_PARAM = "state";

    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> create(long userId, Object requestDto) {
        return post(ROOT_PATH, userId, requestDto);
    }

    public ResponseEntity<Object> approve(long userId, Long bookingId, Boolean approved) {
        return patch(APPROVE_PATH_TEMPLATE.formatted(bookingId), userId, Map.of(APPROVED_PARAM, approved), null);
    }

    public ResponseEntity<Object> getById(long userId, Long bookingId) {
        return get(ID_PATH_PREFIX + bookingId, userId);
    }

    public ResponseEntity<Object> getUserBookings(long userId, BookingState state) {
        return get(USER_BOOKINGS_PATH, userId, Map.of(STATE_PARAM, state.name()));
    }

    public ResponseEntity<Object> getOwnerBookings(long userId, BookingState state) {
        return get(OWNER_BOOKINGS_PATH, userId, Map.of(STATE_PARAM, state.name()));
    }
}
