package ru.practicum.shareit;

import jakarta.validation.Validator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.item.dto.ItemShortForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

@JsonTest
public class ItemRequestDtoJsonTest {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Autowired
    private JacksonTester<ItemRequestRequestDto> itemRequestRequestJson;

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

    @Test
    @DisplayName("JSON test 2: вложенные вещи сериализуются с id, name и ownerId")
    void serializeNestedItems() throws Exception {
        ItemRequestResponseDto dto = new ItemRequestResponseDto(
                1L, "Нужна дрель", LocalDateTime.of(2027, 1, 15, 10, 30, 0),
                List.of(new ItemShortForRequestDto(10L, "Дрель", 2L)));

        JsonContent<ItemRequestResponseDto> result = itemRequestResponseJson.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(2);
    }

    @Test
    @DisplayName("JSON test 3: пустой список вещей сериализуется как [], а не null")
    void serializeEmptyItems() throws Exception {
        ItemRequestResponseDto dto = new ItemRequestResponseDto();
        dto.setId(1L);

        JsonContent<ItemRequestResponseDto> result = itemRequestResponseJson.write(dto);

        assertThat(result).hasEmptyJsonPathValue("$.items");
        assertThat(result.getJson()).contains("\"items\":[]");
    }

    @Test
    @DisplayName("JSON test 4: created десериализуется из формата yyyy-MM-dd'T'HH:mm:ss")
    void deserializeCreatedFromPattern() throws Exception {
        String content = "{\"id\":7,\"description\":\"Нужна дрель\",\"created\":\"2027-01-15T10:30:45\","
                + "\"items\":[{\"id\":10,\"name\":\"Дрель\",\"ownerId\":2}]}";

        ItemRequestResponseDto result = itemRequestResponseJson.parseObject(content);

        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getDescription()).isEqualTo("Нужна дрель");
        assertThat(result.getCreated()).isEqualTo(LocalDateTime.of(2027, 1, 15, 10, 30, 45));
        assertThat(result.getItems()).containsExactly(new ItemShortForRequestDto(10L, "Дрель", 2L));
    }

    @Test
    @DisplayName("JSON test 5: описание запроса не более 200 символов")
    void itemRequestRequestDescriptionBoundary() throws Exception {
        ItemRequestRequestDto ok = itemRequestRequestJson.parseObject(
                "{\"description\":\"" + "d".repeat(200) + "\"}");
        ItemRequestRequestDto tooLong = itemRequestRequestJson.parseObject(
                "{\"description\":\"" + "d".repeat(201) + "\"}");
        ItemRequestRequestDto blank = itemRequestRequestJson.parseObject("{\"description\":\"   \"}");

        assertThat(VALIDATOR.validate(ok)).isEmpty();
        assertThat(VALIDATOR.validate(tooLong)).hasSize(1);
        assertThat(VALIDATOR.validate(blank)).hasSize(1);
    }
}
