package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCreateDto {

    /** Краткое название вещи */
    @NotBlank(message = "Название не может быть пустым")
    @Size(max = 100, message = "Максимальная длина названия 100 символов")
    private String name;

    /** Развёрнутое описание вещи */
    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = 1000, message = "Максимальная длина описания 1000 символов")
    private String description;

    /** Доступность вещи для аренды */
    @NotNull
    private Boolean available;

    /** Id запроса, по которому создается вещь (опционально) */
    private Long requestId;
}
