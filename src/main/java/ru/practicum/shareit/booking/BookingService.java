package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingStatusDto;

import java.util.List;

public interface BookingService {

    BookingResponseDto createBooking(Long userId, BookingRequestDto bookingRequestDto);

    BookingResponseDto approveBooking(Long userId, Long bookingId, boolean isApproved);

    BookingResponseDto getBooking(Long userId, Long bookingId);

    List<BookingResponseDto> getBookingsForCurrentUser(Long userId, BookingStatusDto state);

    List<BookingResponseDto> getOwnersBookings(Long userId, BookingStatusDto state);
}
