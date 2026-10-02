package ru.practicum.shareit.user;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {

    /** Имя или логин пользователя */
    private String name;

    /** Уникальный адрес электронной почты */
    @Email(message = "Некорректный формат email")
    private String email;
}
