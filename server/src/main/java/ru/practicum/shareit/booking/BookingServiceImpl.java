package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingStatusDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, UserService userService, ItemService itemService) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.itemService = itemService;
    }

    @Override
    @Transactional
    public BookingResponseDto createBooking(Long userId, BookingRequestDto bookingRequestDto) {
        User booker = userService.getValidUser(userId);
        Item item = itemService.getValidItem(bookingRequestDto.getItemId());
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь для бронирования недоступна");
        }
        if (!bookingRequestDto.getEnd().isAfter(bookingRequestDto.getStart())) {
            throw new ValidationException("Дата окончания должна быть позже даты начала");
        }
        if (Objects.equals(item.getOwner().getId(), userId)) {
            throw new ValidationException("Владелец не может бронировать свою вещь");
        }
        Booking newBooking = BookingMapper.toBooking(bookingRequestDto);
        newBooking.setBooker(booker);
        newBooking.setItem(item);
        newBooking.setStatus(BookingStatus.WAITING);
        Booking savedBooking = bookingRepository.save(newBooking);

        return BookingMapper.toResponseDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long userId, Long bookingId, boolean isApproved) {
        Booking booking = getValidBooking(bookingId);

        boolean isOwnersBooking = Objects.equals(booking.getItem().getOwner().getId(), userId);
        if (!isOwnersBooking) {
            throw new ForbiddenException("Пользователь не является владельцем вещи");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Это бронирование недоступно");
        }

        booking.setStatus(isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking approvedBooking = bookingRepository.save(booking);
        return BookingMapper.toResponseDto(approvedBooking);
    }

    @Override
    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        userService.getValidUser(userId);
        Booking booking = getValidBooking(bookingId);

        boolean isOwner = Objects.equals(booking.getItem().getOwner().getId(), userId);
        boolean isBooker = Objects.equals(booking.getBooker().getId(), userId);

        if (!isOwner && !isBooker) {
            throw new ForbiddenException("Доступ для пользователя " + userId + " запрещен");
        }

        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getBookingsForCurrentUser(Long userId, String state) {
        userService.getValidUser(userId);
        BookingStatusDto bookingStatus = stateToBookingStatus(state);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (bookingStatus) {
            case ALL -> bookingRepository.findByBookerIdOrderByStartDesc(userId);
            case CURRENT -> bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
            case PAST -> bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
        };

        return bookings.stream()
                .map(BookingMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<BookingResponseDto> getOwnersBookings(Long userId, String state) {
        userService.getValidUser(userId);
        BookingStatusDto bookingStatus = stateToBookingStatus(state);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (bookingStatus) {
            case ALL -> bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
            case CURRENT -> bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
            case PAST -> bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING -> bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
        };

        return bookings.stream()
                .map(BookingMapper::toResponseDto)
                .toList();
    }

    private Booking getValidBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id = " + bookingId + " не найдено"));
    }

    private BookingStatusDto stateToBookingStatus(String state) {
        try {
            return BookingStatusDto.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Запрос не поддерживается");
        }
    }
}
