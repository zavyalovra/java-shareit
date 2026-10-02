package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * TODO Sprint add-item-requests.
 */
@Data
public class ItemRequestRequestDto {

    /** Текст запроса, содержащий описание требуемой вещи */
    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = 200, message = "Максимальная длина описания 200 символов")
    private String description;
}
