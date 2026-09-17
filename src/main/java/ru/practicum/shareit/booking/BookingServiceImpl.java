package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    public BookingResponseDto createBooking(Long userId, BookingRequestDto bookingRequestDto) {
        User booker = userService.getValidUser(userId);
        Item item = itemService.getValidItem(bookingRequestDto.getItemId());
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь для бронирования недоступна");
        }
        Booking newBooking = BookingMapper.toBooking(bookingRequestDto);
        newBooking.setBooker(booker);
        newBooking.setItem(item);
        newBooking.setStatus(BookingStatus.WAITING);
        Booking savedBooking = bookingRepository.save(newBooking);

        return BookingMapper.toResponseDto(savedBooking);
    }

    @Override
    public BookingResponseDto approveBooking(Long userId, Long bookingId, boolean isApproved) {
        Booking booking = getValidBooking(bookingId);

        boolean isOwnersBooking = Objects.equals(booking.getItem().getOwner().getId(), userId);
        if (!isOwnersBooking) {
            throw new ForbiddenException("Пользователь не является владельцем вещи");
        }

        userService.getValidUser(userId);

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
    public List<BookingResponseDto> getBookingsForCurrentUser(Long userId, BookingStatusDto state) {
        userService.getValidUser(userId);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findAllByBookerId(userId);
            case CURRENT -> bookingRepository.findCurrentByBookerId(userId, now);
            case PAST -> bookingRepository.findPastByBookerId(userId, now);
            case FUTURE -> bookingRepository.findFutureByBookerId(userId, now);
            case WAITING -> bookingRepository.findWaitingByBookerId(userId);
            case REJECTED -> bookingRepository.findRejectedByBookerId(userId);
        };

        return bookings.stream()
                .map(BookingMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<BookingResponseDto> getOwnersBookings(Long userId, BookingStatusDto state) {
        userService.getValidUser(userId);
        LocalDateTime now = LocalDateTime.now();

        if (bookingRepository.findAllOwnersBookings(userId).isEmpty()) {
            return List.of();
        }

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findAllOwnersBookings(userId);
            case CURRENT -> bookingRepository.findCurrentOwnersBookings(userId, now);
            case PAST -> bookingRepository.findPastOwnersBookings(userId, now);
            case FUTURE -> bookingRepository.findFutureOwnersBookings(userId, now);
            case WAITING -> bookingRepository.findWaitingOwnersBookings(userId);
            case REJECTED -> bookingRepository.findRejectedOwnersBookings(userId);
        };

        return bookings.stream()
                .map(BookingMapper::toResponseDto)
                .toList();
    }

    private Booking getValidBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id = " + bookingId + " не найдено"));
    }
}
