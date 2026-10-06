package ru.practicum.shareit.user.dto;

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
    private String email;
}
