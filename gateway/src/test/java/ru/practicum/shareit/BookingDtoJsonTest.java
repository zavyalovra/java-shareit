package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.Validation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;


import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoJsonTest {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Autowired
    private JacksonTester<BookItemRequestDto> bookingRequestJson;

    @Test
    @DisplayName("JSON test 1: BookingRequestDto десериализуется из ISO-8601")
    void bookingRequestDeserialize() throws Exception {
        String content = "{\"itemId\":5,\"start\":\"2027-01-01T10:00:00\",\"end\":\"2027-01-02T12:30:15\"}";

        BookItemRequestDto dto = bookingRequestJson.parseObject(content);

        Assertions.assertThat(dto.getItemId()).isEqualTo(5L);
        Assertions.assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2027, 1, 1, 10, 0, 0));
        Assertions.assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2027, 1, 2, 12, 30, 15));
    }

    @Test
    @DisplayName("JSON test 2: валидный BookingRequestDto не нарушает ограничений")
    void bookingRequestValidHasNoViolations() {
        BookItemRequestDto dto = new BookItemRequestDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        assertThat(VALIDATOR.validate(dto)).isEmpty();
    }

    @Test
    @DisplayName("JSON test 3: start позже end - бросает ошибку валидации")
    void bookingRequestEndInPastViolatesFuture() {
        BookItemRequestDto dto = new BookItemRequestDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusDays(3));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        Set<ConstraintViolation<BookItemRequestDto>> violations = VALIDATOR.validate(dto);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("startBeforeEnd");
    }

    @Test
    @DisplayName("JSON test 4: пустой BookingRequestDto нарушает @NotNull для itemId, start, end")
    void bookingRequestEmptyViolatesNotNull() {
        Set<ConstraintViolation<BookItemRequestDto>> violations = VALIDATOR.validate(new BookItemRequestDto());

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("itemId", "start", "end");
    }
}
