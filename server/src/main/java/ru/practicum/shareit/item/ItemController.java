package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<ItemResponseDto> findOwnerItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getByOwnerId(userId);
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto findItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long itemId) {
        return itemService.getItemById(itemId, userId);
    }

    @PostMapping
    public ItemShortResponseDto createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return itemService.createItem(userId, itemRequestDto);
    }

    @PatchMapping("/{itemId}")
    public ItemShortResponseDto updateItem(@PathVariable Long itemId,
                                           @RequestHeader("X-Sharer-User-Id") Long userId,
                                           @Valid @RequestBody UpdateItemRequestDto itemDto) {
        return itemService.updateItem(itemId, userId, itemDto);
    }

    @GetMapping("/search")
    public List<ItemShortResponseDto> searchItems(@RequestParam String text) {
        return itemService.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto createComment(@PathVariable Long itemId,
                                 @RequestHeader("X-Sharer-User-Id") Long userId,
                                 @Valid @RequestBody CommentRequestDto commentDto) {
        return itemService.createComment(itemId, userId, commentDto);
    }
}
