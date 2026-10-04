package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {

    /** Текст запроса, содержащий описание требуемой вещи */
    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = 1000, message = "Максимальная длина описания 1000 символов")
    private String description;
}
