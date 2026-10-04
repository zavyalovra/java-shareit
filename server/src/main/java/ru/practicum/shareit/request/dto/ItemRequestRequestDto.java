package ru.practicum.shareit.request.dto;

import lombok.Data;

/**
 * TODO Sprint add-item-requests.
 */
@Data
public class ItemRequestRequestDto {

    /** Текст запроса, содержащий описание требуемой вещи */
    private String description;
}
