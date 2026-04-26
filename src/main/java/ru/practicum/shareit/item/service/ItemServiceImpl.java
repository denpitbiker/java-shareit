package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.BadRequestException;
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
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";
    private static final String ITEM_NOT_FOUND_MESSAGE = "Item not found";
    private static final String ITEM_OWNER_ONLY_MESSAGE = "Only the owner can update the item";
    private static final String ITEM_NAME_BLANK_MESSAGE = "Item name must not be blank";
    private static final String ITEM_DESCRIPTION_BLANK_MESSAGE = "Item description must not be blank";

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = getUserOrThrow(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        getUserOrThrow(userId);
        Item existingItem = getItemOrThrow(itemId);
        validateOwner(existingItem, userId);

        if (itemDto.getName() != null) {
            validateName(itemDto.getName());
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            validateDescription(itemDto.getDescription());
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(itemRepository.update(existingItem));
    }

    @Override
    public ItemDto getById(Long itemId) {
        return ItemMapper.toItemDto(getItemOrThrow(itemId));
    }

    @Override
    public List<ItemDto> getOwnerItems(Long userId) {
        getUserOrThrow(userId);
        return itemRepository.findByOwnerId(userId)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text.trim())
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MESSAGE));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND_MESSAGE));
    }

    private void validateOwner(Item item, Long userId) {
        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenOperationException(ITEM_OWNER_ONLY_MESSAGE);
        }
    }

    private void validateName(String name) {
        if (name.isBlank()) {
            throw new BadRequestException(ITEM_NAME_BLANK_MESSAGE);
        }
    }

    private void validateDescription(String description) {
        if (description.isBlank()) {
            throw new BadRequestException(ITEM_DESCRIPTION_BLANK_MESSAGE);
        }
    }
}
