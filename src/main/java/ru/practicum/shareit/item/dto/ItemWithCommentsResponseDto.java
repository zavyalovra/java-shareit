package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.List;

@Data
@AllArgsConstructor
public class ItemWithCommentsResponseDto {

    /** Уникальный идентификатор вещи */
    private Long id;

    /** Краткое название вещи */
    private String name;

    /** Развёрнутое описание вещи */
    private String description;

    /** Доступность вещи для аренды */
    private Boolean available;

    /** Дата последнего бронирования */
    private BookingShortDto lastBooking;

    /** Дата следующего бронирования */
    private BookingShortDto nextBooking;

    /** Комментарии арендаторов вещи */
    private List<CommentResponseDto> comments;
}
