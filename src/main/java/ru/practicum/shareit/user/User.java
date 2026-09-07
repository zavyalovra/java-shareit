package ru.practicum.shareit.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */
@Data
public class User {

    /** Уникальный идентификатор пользователя */
    private Long id;

    /** Имя или логин пользователя */
    @NotBlank(message = "Логин не может быть пустым")
    private String name;

    /** Уникальный адрес электронной почты */
    @NotBlank(message = "Email обязателен для заполнения")
    @Email(message = "Некорректный формат email")
    private String email;
}