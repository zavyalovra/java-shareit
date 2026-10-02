package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDto {
    @NotBlank(message = "Имя не может быть пустым")
    @Size(max = 100, message = "Максимальная длина названия 100 символов")
    private String name;

    @NotNull(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;
}
