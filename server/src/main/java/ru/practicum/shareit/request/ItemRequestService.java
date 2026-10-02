package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestResponseDto addItemRequest(Long userId, ItemRequestRequestDto itemDto);

    List<ItemRequestResponseDto> getUserRequests(Long userId);

    List<ItemRequestResponseDto> getAllRequests(Long userId);

    ItemRequestResponseDto getByRequestId(Long requestId);
}
