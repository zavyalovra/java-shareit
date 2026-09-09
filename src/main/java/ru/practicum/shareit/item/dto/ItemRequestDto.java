package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
public class ItemRequestDto {

    /** Краткое название вещи */
    @NotEmpty
    @NotNull
    private String name;

    /** Развёрнутое описание вещи */
    @NotEmpty
    @NotNull
    private String description;

    /** Доступность вещи для аренды */
    @NotNull
    private Boolean available;
}