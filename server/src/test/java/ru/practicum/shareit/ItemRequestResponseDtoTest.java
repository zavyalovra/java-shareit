package ru.practicum.shareit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class ItemRequestResponseDtoTest {

    @Autowired
    private JacksonTester<ItemRequestResponseDto> itemRequestResponseJson;

    @Test
    @DisplayName("JSON test 1: created сериализуется в формате yyyy-MM-dd'T'HH:mm:ss без долей секунды")
    void serializeCreatedWithoutFractionalSeconds() throws Exception {
        ItemRequestResponseDto dto = new ItemRequestResponseDto(
                1L, "Нужна дрель", LocalDateTime.of(2027, 1, 15,
                10, 30, 45, 123_000_000),
                List.of());

        JsonContent<ItemRequestResponseDto> result = itemRequestResponseJson.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Нужна дрель");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2027-01-15T10:30:45");
    }
}
