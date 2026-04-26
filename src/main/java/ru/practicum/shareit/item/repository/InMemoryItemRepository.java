package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private long nextId = 1L;

    @Override
    public Item save(Item item) {
        item.setId(nextId++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public List<Item> findByOwnerId(Long ownerId) {
        return items.values()
                .stream()
                .filter(item -> item.getOwner() != null && item.getOwner().getId().equals(ownerId))
                .sorted(Comparator.comparing(Item::getId))
                .toList();
    }

    @Override
    public List<Item> search(String text) {
        String normalizedText = text.toLowerCase(Locale.ROOT);
        return items.values()
                .stream()
                .filter(Item::getAvailable)
                .filter(item -> containsIgnoreCase(item.getName(), normalizedText)
                        || containsIgnoreCase(item.getDescription(), normalizedText))
                .sorted(Comparator.comparing(Item::getId))
                .toList();
    }

    private boolean containsIgnoreCase(String source, String text) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(text);
    }
}
