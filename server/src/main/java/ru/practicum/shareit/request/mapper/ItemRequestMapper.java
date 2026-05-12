package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public final class ItemRequestMapper {
    private ItemRequestMapper() {
    }

    public static ItemRequestDto toDto(ItemRequest request, List<ItemRequestItemDto> items) {
        if (request == null) {
            return null;
        }
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                items
        );
    }

    public static ItemRequestItemDto toItemDto(Item item) {
        if (item == null) {
            return null;
        }
        return new ItemRequestItemDto(
                item.getId(),
                item.getName(),
                item.getOwner() != null ? item.getOwner().getId() : null,
                item.getRequest() != null ? item.getRequest().getId() : null
        );
    }

    public static ItemRequest toItem(ItemRequestDto dto, User requestor, LocalDateTime created) {
        if (dto == null) {
            return null;
        }
        return ItemRequest.builder()
                .description(dto.getDescription())
                .requestor(requestor)
                .created(created)
                .build();
    }
}
