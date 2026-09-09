package ru.practicum.shareit.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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

    public void update(ItemRequestDto itemDto) {
        if (itemDto.getName() != null) {
            this.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            this.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            this.setAvailable(itemDto.getAvailable());
        }
    }
}
