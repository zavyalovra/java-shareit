package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequestDto {

    /** Имя или логин пользователя */
    private String name;

    /** Уникальный адрес электронной почты */
    @Email(message = "Некорректный формат email")
    private String email;
}
