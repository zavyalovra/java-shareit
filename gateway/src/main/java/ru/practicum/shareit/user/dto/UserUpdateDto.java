package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {

    @Pattern(regexp = ".*\\S.*", message = "Имя не может быть пустым")
    @Size(max = 100, message = "Максимальная длина названия 100 символов")
    private String name;

    @Email(message = "Некорректный формат email")
    private String email;
}