package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequestDto {

    /** Содержимое комментария */
    @NotBlank(message = "Содержание не может быть пустым")
    @Size(max = 2000, message = "Максимальная длина текста 2000 символов")
    private String text;
}
