package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponseDto {

    /** Уникальный идентификатор пользователя */
    private Long id;

    /** Имя или логин пользователя */
    private String name;

    /** Уникальный адрес электронной почты */
    private String email;
}