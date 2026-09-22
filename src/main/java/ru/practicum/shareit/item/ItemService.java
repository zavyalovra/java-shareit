package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {

    Item getValidItem(Long itemId);

    Item getValidItemByOwnerId(Long itemId, Long userId);

    List<ItemResponseDto> getByOwnerId(Long userId);

    ItemResponseDto getItemById(Long itemId, Long userId);

    List<ItemShortResponseDto> searchItems(String text);

    ItemShortResponseDto createItem(Long userId, ItemRequestDto itemDto);

    ItemShortResponseDto updateItem(Long itemId, Long userId, UpdateItemRequestDto itemDto);

    CommentResponseDto createComment(Long itemId, Long userId, CommentRequestDto commentDto);
}
