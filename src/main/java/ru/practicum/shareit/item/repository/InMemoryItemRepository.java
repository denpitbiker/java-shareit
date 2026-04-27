package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Repository
@Slf4j
public class InMemoryItemRepository implements ItemRepository {
    private static final String LOG_SAVE_ITEM = "Saving item in repository, assigned itemId={}";
    private static final String LOG_UPDATE_ITEM = "Updating item in repository, itemId={}";
    private static final String LOG_FIND_ITEM = "Finding item in repository by itemId={}";
    private static final String LOG_FIND_OWNER_ITEMS = "Finding items in repository by ownerId={}";
    private static final String LOG_SEARCH_ITEMS = "Searching items in repository by text='{}'";

    private final Map<Long, Item> items = new HashMap<>();
    private long nextId = 1L;

    @Override
    public Item save(Item item) {
        item.setId(nextId++);
        items.put(item.getId(), item);
        log.info(LOG_SAVE_ITEM, item.getId());
        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        log.info(LOG_UPDATE_ITEM, item.getId());
        return item;
    }

    @Override
    public Optional<Item> findById(Long itemId) {
        log.info(LOG_FIND_ITEM, itemId);
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public List<Item> findByOwnerId(Long ownerId) {
        log.info(LOG_FIND_OWNER_ITEMS, ownerId);
        return items.values()
                .stream()
                .filter(item -> item.getOwner() != null && item.getOwner().getId().equals(ownerId))
                .toList();
    }

    @Override
    public List<Item> search(String text) {
        log.info(LOG_SEARCH_ITEMS, text);
        String normalizedText = text.toLowerCase(Locale.ROOT);
        return items.values()
                .stream()
                .filter(Item::getAvailable)
                .filter(item -> containsIgnoreCase(item.getName(), normalizedText)
                        || containsIgnoreCase(item.getDescription(), normalizedText))
                .toList();
    }

    private boolean containsIgnoreCase(String source, String text) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(text);
    }
}
