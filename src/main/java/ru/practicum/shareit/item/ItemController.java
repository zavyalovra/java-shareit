package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;
import java.util.Optional;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemResponseDto> findOwnerItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getByOwnerId(userId);
    }

    @GetMapping("/{itemId}")
    public Optional<Item> findItemById(@PathVariable Long itemId) {
        return itemService.getItemById(itemId);
    }

    @PostMapping
    public ItemResponseDto createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return itemService.createItem(userId, itemRequestDto);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto updateItem(@PathVariable Long itemId,
                                      @RequestHeader("X-Sharer-User-Id") Long userId,
                                      @RequestBody ItemRequestDto itemRequestDto) {
        return itemService.updateItem(itemId, userId, itemRequestDto);
    }
}
