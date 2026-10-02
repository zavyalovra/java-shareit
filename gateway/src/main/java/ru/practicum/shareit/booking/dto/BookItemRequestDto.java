package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {

	/** Вещь, которую пользователь бронирует */
	@NotNull
	private Long itemId;

	/** Дата и время начала бронирования */
	@NotNull
	@FutureOrPresent
	private LocalDateTime start;

	/** Дата и время конца бронирования */
	@NotNull
	@Future
	private LocalDateTime end;
}
