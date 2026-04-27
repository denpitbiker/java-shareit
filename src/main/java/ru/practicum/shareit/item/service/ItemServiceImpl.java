package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.ForbiddenOperationException;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";
    private static final String ITEM_NOT_FOUND_MESSAGE = "Item not found";
    private static final String ITEM_OWNER_ONLY_MESSAGE = "Only the owner can update the item";
    private static final String LOG_CREATE_ITEM = "Creating item in service for userId={}, itemDto={}";
    private static final String LOG_UPDATE_ITEM = "Updating item in service: itemId={}, userId={}, itemDto={}";
    private static final String LOG_GET_ITEM = "Getting item in service by itemId={}";
    private static final String LOG_GET_OWNER_ITEMS = "Getting owner items in service for userId={}";
    private static final String LOG_SEARCH_ITEMS = "Searching items in service by text='{}'";
    private static final String LOG_BLANK_SEARCH = "Search text is blank, returning empty result";
    private static final String LOG_USER_NOT_FOUND = "User not found for userId={}";
    private static final String LOG_ITEM_NOT_FOUND = "Item not found for itemId={}";
    private static final String LOG_OWNER_VALIDATION_FAILED = "UserId={} is not owner of itemId={}";

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info(LOG_CREATE_ITEM, userId, itemDto);
        User owner = getUserOrThrow(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        log.info(LOG_UPDATE_ITEM, itemId, userId, itemDto);
        getUserOrThrow(userId);
        Item existingItem = getItemOrThrow(itemId);
        validateOwner(existingItem, userId);

        if (validateName(itemDto.getName())) {
            existingItem.setName(itemDto.getName());
        }
        if (validateDescription(itemDto.getDescription())) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(itemRepository.update(existingItem));
    }

    @Override
    public ItemDto getById(Long itemId) {
        log.info(LOG_GET_ITEM, itemId);
        return ItemMapper.toItemDto(getItemOrThrow(itemId));
    }

    @Override
    public List<ItemDto> getOwnerItems(Long userId) {
        log.info(LOG_GET_OWNER_ITEMS, userId);
        getUserOrThrow(userId);
        return itemRepository.findByOwnerId(userId)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info(LOG_SEARCH_ITEMS, text);
        if (text == null || text.isBlank()) {
            log.info(LOG_BLANK_SEARCH);
            return List.of();
        }
        return itemRepository.search(text.trim())
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn(LOG_USER_NOT_FOUND, userId);
                    return new NotFoundException(USER_NOT_FOUND_MESSAGE);
                });
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn(LOG_ITEM_NOT_FOUND, itemId);
                    return new NotFoundException(ITEM_NOT_FOUND_MESSAGE);
                });
    }

    private void validateOwner(Item item, Long userId) {
        if (!item.getOwner().getId().equals(userId)) {
            log.warn(LOG_OWNER_VALIDATION_FAILED, userId, item.getId());
            throw new ForbiddenOperationException(ITEM_OWNER_ONLY_MESSAGE);
        }
    }

    private boolean validateName(String name) {
        return name != null && !name.isBlank();
    }

    private boolean validateDescription(String description) {
        return description != null && !description.isBlank();
    }
}
