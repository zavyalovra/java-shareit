package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public ItemServiceImpl(
            UserService userService,
            ItemRepository itemRepository,
            BookingRepository bookingRepository,
            CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userService = userService;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public List<ItemOwnersResponseDto> getByOwnerId(Long userId) {
        User owner = userService.getValidUser(userId);
        List<Item> items = itemRepository.findByOwnerId(owner.getId());

        if (items.isEmpty()) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();
        List<Long> itemsIds = items.stream()
                .map(Item::getId)
                .toList();

        Map<Long, Booking> lastByItem = bookingRepository.findLastBooking(itemsIds, now).stream()
                .collect(Collectors.toMap(b -> b.getItem().getId(), b -> b, (a, b) -> a));

        Map<Long, Booking> nextByItem = bookingRepository.findNextBooking(itemsIds, now).stream()
                .collect(Collectors.toMap(b -> b.getItem().getId(), b -> b, (a, b) -> a));

        return items.stream()
                .map(item -> ItemMapper.toItemOwnersResponseDto(
                        item,
                        toShortDto(lastByItem.get(item.getId())),
                        toShortDto(nextByItem.get(item.getId()))
                        ))
                        .toList();
    }

    private BookingShortDto toShortDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return new BookingShortDto(booking.getStart(), booking.getEnd());
    }

    @Override
    public ItemWithCommentsResponseDto getItemById(Long itemId, Long userId) {
        Item item = getValidItem(itemId);
        LocalDateTime now = LocalDateTime.now();
        BookingShortDto lastBooking = null;
        BookingShortDto nextBooking = null;

        List<CommentResponseDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(ItemMapper::toCommentResponseDto)
                .toList();

        if (item.getOwner().getId().equals(userId)) {
            lastBooking = bookingRepository.findLastBooking(List.of(item.getId()), now).stream()
                    .findFirst()
                    .map(this::toShortDto)
                    .orElse(null);

            nextBooking = bookingRepository.findNextBooking(List.of(item.getId()), now).stream()
                    .findFirst()
                    .map(this::toShortDto)
                    .orElse(null);
        }

        return ItemMapper.toWithCommentsResponseDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public ItemResponseDto createItem(Long userId, ItemRequestDto itemDto) {
        User owner = userService.getValidUser(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        Item savedItem = itemRepository.save(item);

        return ItemMapper.toResponseDto(savedItem);
    }

    @Override
    public ItemResponseDto updateItem(Long itemId, Long userId, UpdateItemRequestDto itemDto) {
        Item item = getValidItemByOwnerId(itemId, userId);
        ItemMapper.applyUpdate(item, itemDto);
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

        if (!item.getOwner().getId().equals(owner.getId())) {
            throw new ForbiddenException("Пользователь не владелец вещи");
        }

        return item;
    }

    @Override
    public CommentResponseDto createComment(Long itemId, Long userId, CommentRequestDto commentDto) {
        if (!validateBooking(itemId, userId)) {
            throw new ValidationException("Пользователь не найден в истории брони этой вещи");
        }

        Item item = getValidItem(itemId);
        User author = userService.getValidUser(userId);
        Comment comment = CommentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);

        return CommentMapper.toResponseDto(savedComment);
    }

    private boolean validateBooking(Long itemId, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        Optional<Booking> existedBooking = bookingRepository.findCompletedBookings(itemId, userId, now)
                .stream()
                .findFirst();

        return existedBooking.isPresent();
    }
}