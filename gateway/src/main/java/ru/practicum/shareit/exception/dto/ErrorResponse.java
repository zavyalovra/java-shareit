package ru.practicum.shareit.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    String error;       // название ошибки
    String description; // подробное описание
}
