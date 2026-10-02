package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemShortResponseDto;
import ru.practicum.shareit.user.UserResponseDto;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoJsonTest {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Autowired
    private JacksonTester<BookingRequestDto> bookingRequestJson;

    @Autowired
    private JacksonTester<BookingResponseDto> bookingResponseJson;

    @Autowired
    private JacksonTester<BookingShortDto> bookingShortJson;

    @Test
    @DisplayName("JSON test 1: BookingRequestDto десериализуется из ISO-8601")
    void bookingRequestDeserialize() throws Exception {
        String content = "{\"itemId\":5,\"start\":\"2027-01-01T10:00:00\",\"end\":\"2027-01-02T12:30:15\"}";

        BookingRequestDto dto = bookingRequestJson.parseObject(content);

        assertThat(dto.getItemId()).isEqualTo(5L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2027, 1, 1, 10, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2027, 1, 2, 12, 30, 15));
    }

    @Test
    @DisplayName("JSON test 2: валидный BookingRequestDto не нарушает ограничений")
    void bookingRequestValidHasNoViolations() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        assertThat(VALIDATOR.validate(dto)).isEmpty();
    }

    @Test
    @DisplayName("JSON test 3: end в прошлом нарушает @Future")
    void bookingRequestEndInPastViolatesFuture() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().minusDays(3));
        dto.setEnd(LocalDateTime.now().minusDays(2));

        Set<ConstraintViolation<BookingRequestDto>> violations = VALIDATOR.validate(dto);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("end");
    }

    @Test
    @DisplayName("JSON test 4: пустой BookingRequestDto нарушает @NotNull для itemId, start, end")
    void bookingRequestEmptyViolatesNotNull() {
        Set<ConstraintViolation<BookingRequestDto>> violations = VALIDATOR.validate(new BookingRequestDto());

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("itemId", "start", "end");
    }

    @Test
    @DisplayName("JSON test 5: BookingResponseDto сериализует даты в ISO-8601, статус именем enum и вложенные объекты")
    void bookingResponseSerialize() throws Exception {
        BookingResponseDto dto = new BookingResponseDto(
                1L,
                LocalDateTime.of(2027, 1, 1, 10, 0, 0),
                LocalDateTime.of(2027, 1, 2, 10, 0, 0),
                BookingStatus.APPROVED,
                new UserResponseDto(2L, "Dominica", "dominica@beluchi.com"),
                new ItemShortResponseDto(5L, "Дрель", "Мощная дрель", true));

        JsonContent<BookingResponseDto> result = bookingResponseJson.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2027-01-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2027-01-02T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.booker.email").isEqualTo("dominica@beluchi.com");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(5);
        assertThat(result).extractingJsonPathBooleanValue("$.item.available").isTrue();
    }

    @Test
    @DisplayName("JSON test 6: BookingShortDto содержит только start и end")
    void bookingShortSerialize() throws Exception {
        BookingShortDto dto = new BookingShortDto(
                LocalDateTime.of(2027, 1, 1, 10, 0, 0), LocalDateTime.of(2027, 1, 2, 10, 0, 0));

        JsonContent<BookingShortDto> result = bookingShortJson.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2027-01-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2027-01-02T10:00:00");
        assertThat(result.getJson()).doesNotContain("id");
    }
}
