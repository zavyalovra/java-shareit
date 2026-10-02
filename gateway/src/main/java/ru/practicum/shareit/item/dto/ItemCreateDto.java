package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemCreateDto {

    /** Краткое название вещи */
    @NotBlank(message = "Название не может быть пустым")
    @Size(max = 100, message = "Максимальная длина названия 100 символов")
    private String name;

    /** Развёрнутое описание вещи */
    @NotEmpty
    @NotNull
    private String description;

    /** Доступность вещи для аренды */
    @NotNull
    private Boolean available;

    /** Id запроса, по которому создается вещь (опционально) */
    private Long requestId;
}
