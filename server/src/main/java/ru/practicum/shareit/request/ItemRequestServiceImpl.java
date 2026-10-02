package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;

    @Autowired
    public ItemRequestServiceImpl(ItemRequestRepository itemRequestRepository, UserService userService) {
        this.itemRequestRepository = itemRequestRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public ItemRequestResponseDto addItemRequest(Long userId, ItemRequestRequestDto requestDto) {
        User requestor = userService.getValidUser(userId);
        if (itemRequestRepository.existsByRequestorIdAndDescription(userId, requestDto.getDescription())) {
            throw new ConflictException("Пользователь уже создал этот запрос");
        }
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(requestDto);
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());
        ItemRequest savedItemRequest = itemRequestRepository.save(itemRequest);

        return ItemRequestMapper.toItemRequestDto(savedItemRequest);
    }

    @Override
    public List<ItemRequestResponseDto> getUserRequests(Long userId) {
        User user = userService.getValidUser(userId);
        List<ItemRequest> itemRequestList = itemRequestRepository.findByRequestor_IdOrderByCreatedDesc(userId);
        return itemRequestList.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId) {
        User user = userService.getValidUser(userId);
        List<ItemRequest> itemRequestList = itemRequestRepository.findByRequestor_IdNotOrderByCreatedDesc(userId);
        return itemRequestList.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();
    }

    @Override
    public ItemRequestResponseDto getByRequestId(Long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }
}
