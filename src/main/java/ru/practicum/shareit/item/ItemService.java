package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;
import java.util.Optional;

public interface ItemService {

    List<ItemResponseDto> getByOwnerId(Long userId);

    Optional<Item> getItemById(Long itemId);

    List<ItemResponseDto> searchItems(String text);

    ItemResponseDto createItem(Long userId, ItemRequestDto itemDto);

    ItemResponseDto updateItem(Long itemId, Long userId, ItemRequestDto itemDto);
}
