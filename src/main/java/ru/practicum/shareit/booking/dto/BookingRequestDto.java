package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequestDto {

    /** Вещь, которую пользователь бронирует */
    @NotNull
    private Long itemId;

    /** Дата и время начала бронирования */
    @NotNull
    @Future
    private LocalDateTime start;

    /** Дата и время конца бронирования */
    @NotNull
    @Future
    private LocalDateTime end;
}
