package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Autowired
    public ItemRequestServiceImpl(ItemRequestRepository itemRequestRepository, UserService userService, ItemRepository itemRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.userService = userService;
        this.itemRepository = itemRepository;
    }

    @Override
    @Transactional
    public ItemRequestResponseDto addItemRequest(Long userId, ItemRequestRequestDto requestDto) {
        User requestor = userService.getValidUser(userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(requestDto);
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());
        ItemRequest savedItemRequest = itemRequestRepository.save(itemRequest);

        return ItemRequestMapper.toItemRequestDto(savedItemRequest);
    }

    @Override
    public List<ItemRequestResponseDto> getUserRequests(Long userId) {
        userService.getValidUser(userId);
        List<ItemRequest> itemRequestList = itemRequestRepository.findByRequestor_IdOrderByCreatedDesc(userId);
        return itemRequestList.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId, int from, int size) {
        userService.getValidUser(userId);

        Pageable page = PageRequest.of(from / size, size,
                Sort.by(Sort.Direction.DESC, "created").and(Sort.by("id")));

        List<ItemRequest> itemRequestList = itemRequestRepository.findByRequestor_IdNot(userId, page);
        if (itemRequestList.isEmpty()) {
            return List.of();
        }

        List<Long> ids = itemRequestList.stream()
                .map(ItemRequest::getId)
                .toList();
        Map<Long, List<Item>> itemsByRequestId = itemRepository.findByRequestIdIn(ids).stream()
                .collect(Collectors.groupingBy(i -> i.getRequest().getId()));

        return itemRequestList.stream()
                .map(r -> ItemRequestMapper.toItemRequestDto(
                        r, itemsByRequestId.getOrDefault(r.getId(), List.of())))
                .toList();
    }

    @Override
    public ItemRequestResponseDto getByRequestId(Long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }
}
