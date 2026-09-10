package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.UpdateItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Autowired
    public ItemServiceImpl(
            UserService userService,
            @Qualifier("InMemoryItemRepository") ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
        this.userService = userService;
    }

    @Override
    public List<ItemResponseDto> getByOwnerId(Long userId) {
        User owner = userService.getValidUser(userId);

        return itemRepository.findByOwnerId(owner.getId()).stream()
                .map(ItemMapper::toResponseDto)
                .toList();
    }

    @Override
    public ItemResponseDto getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        return ItemMapper.toResponseDto(item);
    }

    @Override
    public ItemResponseDto createItem(Long userId, ItemRequestDto itemDto) {
        User owner = userService.getValidUser(userId);
        Item item = ItemMapper.toItem(owner, itemDto);
        Item createdItem = itemRepository.create(item);

        return ItemMapper.toResponseDto(createdItem);
    }

    @Override
    public ItemResponseDto updateItem(Long itemId, Long userId, UpdateItemRequestDto itemDto) {
        Item item = getValidItemByOwnerId(itemId, userId);
        item.update(itemDto);
        Item savedItem = itemRepository.save(item);

        return ItemMapper.toResponseDto(savedItem);
    }

    @Override
    public List<ItemResponseDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<Item> searchResult = itemRepository.search(text);

        return searchResult.stream()
                .map(ItemMapper::toResponseDto)
                .toList();
    }

    @Override
    public Item getValidItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));
    }

    @Override
    public Item getValidItemByOwnerId(Long itemId, Long userId) {
        Item item = getValidItem(itemId);
        User owner = userService.getValidUser(userId);

        if (!item.getOwner().equals(owner)) {
            throw new ValidationException("Пользователь " + item.getOwner().getName() + " не владелец вещи");
        }

        return item;
    }
}