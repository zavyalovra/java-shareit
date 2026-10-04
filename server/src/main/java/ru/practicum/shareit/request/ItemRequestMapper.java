package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dto.ItemShortForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestRequestDto itemRequestRequestDto) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestRequestDto.getDescription());
        return itemRequest;
    }

    public static ItemRequestResponseDto toItemRequestDto(ItemRequest itemRequest) {
        ItemRequestResponseDto itemRequestDto = new ItemRequestResponseDto();
        itemRequestDto.setId(itemRequest.getId());
        itemRequestDto.setDescription(itemRequest.getDescription());
        itemRequestDto.setCreated(itemRequest.getCreated());
        itemRequestDto.setItems(itemRequest.getItems().stream()
                .map(i -> new ItemShortForRequestDto(i.getId(), i.getName(), i.getOwner().getId()))
                .toList());

        return itemRequestDto;
    }

    public static ItemRequestResponseDto toItemRequestDto(ItemRequest itemRequest, List<Item> items) {
        ItemRequestResponseDto itemRequestDto = new ItemRequestResponseDto();
        itemRequestDto.setId(itemRequest.getId());
        itemRequestDto.setDescription(itemRequest.getDescription());
        itemRequestDto.setCreated(itemRequest.getCreated());
        itemRequestDto.setItems(items.stream()
                .map(i -> new ItemShortForRequestDto(i.getId(), i.getName(), i.getOwner().getId()))
                .toList());
        return itemRequestDto;
    }
}
