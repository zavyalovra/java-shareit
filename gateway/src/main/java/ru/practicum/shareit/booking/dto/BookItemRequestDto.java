package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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

	@JsonIgnore
	@AssertTrue(message = "Время начала не может быть позже конца аренды")
	public boolean isStartBeforeEnd() {
		if (start == null || end == null) {
			return true;
		}
		return start.isBefore(end);
	}
}
