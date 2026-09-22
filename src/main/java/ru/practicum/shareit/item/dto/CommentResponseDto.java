package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentResponseDto {

    /** Уникальный идентификатор комментария */
    private Long id;

    /** Содержимое комментария */
    private String text;

    /** Автор комментария */
    private String authorName;

    /** Дата создания комментария */
    private LocalDateTime created;
}
