package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@AllArgsConstructor
public class BookingResponseDto {

    /** Уникальный идентификатор бронирования */
    private Long id;

    /** Дата и время начала бронирования */
    private LocalDateTime start;

    /** Дата и время конца бронирования */
    private LocalDateTime end;

    /** Статус бронирования */
    private BookingStatus status;

    /** Id пользователя, который осуществляет бронирование */
    private UserResponseDto booker;

    /** Id вещи, которую пользователь бронирует */
    private ItemResponseDto item;
}
