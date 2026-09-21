package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {

    Item getValidItem(Long itemId);

    Item getValidItemByOwnerId(Long itemId, Long userId);

    List<ItemOwnersResponseDto> getByOwnerId(Long userId);

    ItemWithCommentsResponseDto getItemById(Long itemId, Long userId);

    List<ItemResponseDto> searchItems(String text);

    ItemResponseDto createItem(Long userId, ItemRequestDto itemDto);

    ItemResponseDto updateItem(Long itemId, Long userId, UpdateItemRequestDto itemDto);

    CommentResponseDto createComment(Long itemId, Long userId, CommentRequestDto commentDto);
}
