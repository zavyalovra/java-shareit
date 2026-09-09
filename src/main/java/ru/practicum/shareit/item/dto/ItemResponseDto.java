package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemResponseDto {

    /** Уникальный идентификатор вещи */
    private long id;

    /** Краткое название вещи */
    private String name;

    /** Развёрнутое описание вещи */
    private String description;

    /** Доступность вещи для аренды */
    private Boolean available;
}
