package ru.practicum.shareit.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Data
public class ItemRequest {

    /** Уникальный идентификатор запроса */
    private Long id;

    /** Текст запроса, содержащий описание требуемой вещи */
    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = 200, message = "Максимальная длина описания 200 символов")
    private String description;

    /** Пользователь, создавший запрос */
    private User requestor;

    /** Дата и время создания запроса */
    private LocalDateTime created;
}
