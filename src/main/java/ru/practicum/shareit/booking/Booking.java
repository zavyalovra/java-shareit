package ru.practicum.shareit.booking;

import lombok.Data;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
public class Booking {

    /** Уникальный идентификатор бронирования */
    private Long id;

    /** Дата и время начала бронирования */
    private LocalDateTime start;

    /** Дата и время конца бронирования */
    private LocalDateTime end;

    /** Вещь, которую пользователь бронирует */
    private Item item;

    /** Пользователь, который осуществляет бронирование */
    private User booker;

    /** Статус бронирования */
    private BookingStatus status;

    /**
     * Статус бронирования
     */
    public enum BookingStatus {
        WAITING,
        APPROVED,
        REJECTED,
        CANCELED
    }
}
