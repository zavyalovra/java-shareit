package ru.practicum.shareit.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

/**
 * TODO Sprint add-controllers.
 */
@Data
public class Item {

    /** Уникальный идентификатор вещи */
    private long id;

    /** Краткое название вещи */
    @NotBlank(message = "Название не может быть пустым")
    @Size(max = 100, message = "Максимальная длина названия 100 символов")
    private String name;

    /** Развёрнутое описание вещи */
    private String description;

    /** Доступность вещи для аренды */
    private Boolean available;

    /** Владелец вещи */
    @NotBlank(message = "Владелец должен быть указан")
    private User owner;

    /** Запрос, по которому создана вещь */
    private ItemRequest request;
}
