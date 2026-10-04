package ru.practicum.shareit.item.dto;

import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */
@Data
public class ItemRequestDto {

    /** Краткое название вещи */
    private String name;

    /** Развёрнутое описание вещи */
    private String description;

    /** Доступность вещи для аренды */
    private Boolean available;

    /** Id запроса, по которому создается вещь (опционально) */
    private Long requestId;
}