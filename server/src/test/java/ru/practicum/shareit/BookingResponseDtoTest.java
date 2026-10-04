package ru.practicum.shareit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.ItemShortResponseDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class BookingResponseDtoTest {

    @Autowired
    private JacksonTester<BookingResponseDto> bookingResponseJson;

    @Autowired
    private JacksonTester<BookingShortDto> bookingShortJson;

    @Test
    @DisplayName("JSON test 1: BookingResponseDto сериализует даты в ISO-8601, статус именем enum и вложенные объекты")
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
    @DisplayName("JSON test 2: BookingShortDto содержит только start и end")
    void bookingShortSerialize() throws Exception {
        BookingShortDto dto = new BookingShortDto(
                LocalDateTime.of(2027, 1, 1, 10, 0, 0), LocalDateTime.of(2027, 1, 2, 10, 0, 0));

        JsonContent<BookingShortDto> result = bookingShortJson.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2027-01-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2027-01-02T10:00:00");
        assertThat(result.getJson()).doesNotContain("id");
    }
}
