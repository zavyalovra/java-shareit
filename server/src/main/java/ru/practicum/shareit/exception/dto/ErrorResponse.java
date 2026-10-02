package ru.practicum.shareit.exception.dto;

import lombok.Getter;

@Getter
public class ErrorResponse {
    String error;       // название ошибки
    String description; // подробное описание

    public ErrorResponse(String error, String description) {
        this.error = error;
        this.description = description;
    }
}
