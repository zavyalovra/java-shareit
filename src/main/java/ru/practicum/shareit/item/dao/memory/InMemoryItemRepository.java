package ru.practicum.shareit.item.dao.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;

import java.util.*;

@Repository
@Qualifier("InMemoryItemRepository")
@Slf4j
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public List<Item> findByOwnerId(Long userId) {
        log.debug("Запрос вещей владельца с id: {}", userId);

        return items.values().stream()
                .filter(item -> item.getOwner() != null)
                .filter(item -> Objects.equals(item.getOwner().getId(), userId))
                .toList();
    }

    @Override
    public Optional<Item> findById(Long itemId) {
        log.info("Запрос данных вещи с id: {}", itemId);
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public Item create(Item item) {
        item.setId(getNextId());
        items.put(item.getId(), item);

        log.info("Создание вещи: {}", item);
        return item;
    }

    @Override
    public Item save(Item item) {
        items.put(item.getId(), item);
        log.info("Обновление данных вещи: {}", item);

        return item;
    }

    @Override
    public List<Item> search(String text) {
        String pattern = text.toLowerCase();

        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> matchIfPresent(item.getName(), pattern)
                                  || matchIfPresent(item.getDescription(), pattern))
                .toList();
    }

    private boolean matchIfPresent(String value, String pattern) {
        if (value == null) {
            return false;
        }
        return value.toLowerCase().contains(pattern);
    }

    private long getNextId() {
        long currentMaxId = items.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
