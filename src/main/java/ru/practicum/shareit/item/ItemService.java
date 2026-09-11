package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.UpdateItemRequestDto;

import java.util.List;

public interface ItemService {

    Item getValidItemByOwnerId(Long itemId, Long userId);

    List<ItemResponseDto> getByOwnerId(Long userId);

    ItemResponseDto getItemById(Long itemId);

    List<ItemResponseDto> searchItems(String text);

    ItemResponseDto createItem(Long userId, ItemRequestDto itemDto);

    ItemResponseDto updateItem(Long itemId, Long userId, UpdateItemRequestDto itemDto);
}
