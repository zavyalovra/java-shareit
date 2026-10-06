package ru.practicum.shareit.booking.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequestDto {

    /** Вещь, которую пользователь бронирует */
    private Long itemId;

    /** Дата и время начала бронирования */
    private LocalDateTime start;

    /** Дата и время конца бронирования */
    private LocalDateTime end;
}
