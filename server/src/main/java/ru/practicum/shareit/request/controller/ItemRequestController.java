package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static ru.practicum.shareit.common.Header.USER_ID_HEADER;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private static final String LOG_CREATE_REQUEST = "Creating item request for userId={}, requestDto={}";
    private static final String LOG_GET_OWN_REQUESTS = "Getting own item requests for userId={}";
    private static final String LOG_GET_ALL_REQUESTS = "Getting all foreign item requests for userId={}";
    private static final String LOG_GET_REQUEST_BY_ID = "Getting item request by requestId={} for userId={}";

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                                 @Valid @RequestBody ItemRequestDto requestDto) {
        log.info(LOG_CREATE_REQUEST, userId, requestDto);
        return itemRequestService.create(userId, requestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getOwnRequests(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info(LOG_GET_OWN_REQUESTS, userId);
        return itemRequestService.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info(LOG_GET_ALL_REQUESTS, userId);
        return itemRequestService.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@RequestHeader(USER_ID_HEADER) Long userId,
                                  @PathVariable Long requestId) {
        log.info(LOG_GET_REQUEST_BY_ID, requestId, userId);
        return itemRequestService.getById(userId, requestId);
    }
}
