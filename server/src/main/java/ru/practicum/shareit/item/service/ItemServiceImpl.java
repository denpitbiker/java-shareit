package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.ForbiddenOperationException;
import ru.practicum.shareit.error.BadRequestException;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";
    private static final String ITEM_NOT_FOUND_MESSAGE = "Item not found";
    private static final String ITEM_REQUEST_NOT_FOUND_MESSAGE = "Item request not found";
    private static final String ITEM_OWNER_ONLY_MESSAGE = "Only the owner can update the item";
    private static final String COMMENT_TEXT_BLANK_MESSAGE = "Comment text must not be blank";
    private static final String COMMENT_FORBIDDEN_MESSAGE = "Only users who completed an approved booking can comment";
    private static final String LOG_CREATE_ITEM = "Creating item in service for userId={}, itemDto={}";
    private static final String LOG_UPDATE_ITEM = "Updating item in service: itemId={}, userId={}, itemDto={}";
    private static final String LOG_ADD_COMMENT = "Adding comment for itemId={} by userId={}, commentDto={}";
    private static final String LOG_GET_ITEM = "Getting item in service by itemId={}";
    private static final String LOG_GET_OWNER_ITEMS = "Getting owner items in service for userId={}";
    private static final String LOG_SEARCH_ITEMS = "Searching items in service by text='{}'";
    private static final String LOG_USER_NOT_FOUND = "User not found for userId={}";
    private static final String LOG_ITEM_NOT_FOUND = "Item not found for itemId={}";
    private static final String LOG_OWNER_VALIDATION_FAILED = "UserId={} is not owner of itemId={}";

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info(LOG_CREATE_ITEM, userId, itemDto);
        User owner = getUserOrThrow(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        if (itemDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException(ITEM_REQUEST_NOT_FOUND_MESSAGE));
            item.setRequest(request);
        }
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

        return ItemMapper.toItemDto(itemRepository.save(existingItem));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getById(Long itemId) {
        return getById(itemId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getById(Long itemId, Long userId) {
        log.info(LOG_GET_ITEM, itemId);
        Item item = getItemOrThrow(itemId);
        Map<Long, List<CommentDto>> commentsByItemId = getCommentsByItemId(List.of(item));

        Map<Long, List<BookingDto>> bookingsByItemId = getBookingsByItemId(List.of(item));
        return toDetailedItemDto(
                item,
                userId,
                commentsByItemId.getOrDefault(item.getId(), List.of()),
                bookingsByItemId.getOrDefault(item.getId(), List.of())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getOwnerItems(Long userId) {
        log.info(LOG_GET_OWNER_ITEMS, userId);
        getUserOrThrow(userId);
        List<Item> items = itemRepository.findByOwner_IdOrderByIdAsc(userId);
        Map<Long, List<BookingDto>> bookingsByItemId = getBookingsByItemId(items);
        Map<Long, List<CommentDto>> commentsByItemId = getCommentsByItemId(items);

        return items
                .stream()
                .map(item -> toDetailedItemDto(
                        item,
                        userId,
                        commentsByItemId.getOrDefault(item.getId(), List.of()),
                        bookingsByItemId.getOrDefault(item.getId(), List.of())
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> search(String text) {
        log.info(LOG_SEARCH_ITEMS, text);
        return itemRepository.search(text.trim())
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        log.info(LOG_ADD_COMMENT, itemId, userId, commentDto);
        User author = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);
        validateComment(commentDto);

        boolean hasCompletedBooking = bookingRepository.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(
                itemId,
                userId,
                BookingStatus.APPROVED,
                LocalDateTime.now()
        );
        if (!hasCompletedBooking) {
            throw new BadRequestException(COMMENT_FORBIDDEN_MESSAGE);
        }

        Comment comment = CommentMapper.toComment(commentDto, item, author, LocalDateTime.now());

        return CommentMapper.toCommentDto(commentRepository.save(comment));
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

    private void validateComment(CommentDto commentDto) {
        if (commentDto == null || commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new BadRequestException(COMMENT_TEXT_BLANK_MESSAGE);
        }
    }

    private ItemDto toDetailedItemDto(Item item, Long userId, List<CommentDto> comments, List<BookingDto> bookings) {
        ItemDto itemDto = ItemMapper.toItemDto(item);
        itemDto.setComments(comments);
        if (item.getOwner() != null && item.getOwner().getId().equals(userId) && !bookings.isEmpty()) {
            itemDto.setNextBooking(BookingMapper.toBookingShortDto(bookings.getFirst()));
            itemDto.setLastBooking(BookingMapper.toBookingShortDto(bookings.getLast()));
        }
        return itemDto;
    }

    private Map<Long, List<BookingDto>> getBookingsByItemId(List<Item> items) {
        return bookingRepository.findApprovedByItemInOrderByStartAsc(items)
                .stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getItem().getId(),
                        Collectors.mapping(BookingMapper::toBookingDto, Collectors.toList())
                ));
    }

    private Map<Long, List<CommentDto>> getCommentsByItemId(List<Item> items) {
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();
        if (itemIds.isEmpty()) {
            return Map.of();
        }
        return commentRepository.findByItem_IdInOrderByCreatedAsc(itemIds)
                .stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentDto, Collectors.toList())
                ));
    }
}
