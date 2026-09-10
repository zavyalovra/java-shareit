package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.UpdateItemRequestDto;
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
    private String name;

    /** Развёрнутое описание вещи */
    private String description;

    /** Доступность вещи для аренды */
    private Boolean available;

    /** Владелец вещи */
    private User owner;

    /** Запрос, по которому создана вещь */
    private ItemRequest request;

    public void update(UpdateItemRequestDto itemDto) {
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
