package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
public class UpdateItemRequestDto {

    /** Краткое название вещи */
    @Pattern(regexp = ".*\\S.*", message = "Имя не может быть пустым")
    @Size(max = 100, message = "Максимальная длина имени 100 символов")
    private String name;

    /** Развёрнутое описание вещи */
    private String description;

    /** Доступность вещи для аренды */
    private Boolean available;
}