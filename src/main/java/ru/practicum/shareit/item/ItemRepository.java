package ru.practicum.shareit.item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {

    List<Item> findByOwnerId(Long userId);

    Optional<Item> findById(Long itemId);

    Item create(Item item);

    Item save(Item item);
}
