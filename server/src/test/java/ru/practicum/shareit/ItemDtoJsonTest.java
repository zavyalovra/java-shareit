package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@JsonTest
public class ItemDtoJsonTest {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Autowired
    private JacksonTester<ItemRequestDto> itemRequestJson;

    @Autowired
    private JacksonTester<UpdateItemRequestDto> updateItemJson;

    @Autowired
    private JacksonTester<CommentRequestDto> commentRequestJson;

    @Autowired
    private JacksonTester<CommentResponseDto> commentResponseJson;

    @Autowired
    private JacksonTester<ItemResponseDto> itemResponseJson;

    private <T> List<String> invalidFields(T dto) {
        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(dto);
        return violations.stream()
                .map(v -> v.getPropertyPath().toString()).distinct()
                .toList();
    }

    @Test
    @DisplayName("JSON test 1: ItemRequestDto без requestId десериализуется, requestId = null")
    void itemRequestOptionalRequestId() throws Exception {
        ItemRequestDto dto = itemRequestJson.parseObject(
                "{\"name\":\"Дрель\",\"description\":\"Мощная\",\"available\":true}");

        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isNull();
        assertThat(invalidFields(dto)).isEmpty();
    }

    @Test
    @DisplayName("JSON test 2: ItemRequestDto с requestId десериализуется")
    void itemRequestWithRequestId() throws Exception {
        ItemRequestDto dto = itemRequestJson.parseObject(
                "{\"name\":\"Дрель\",\"description\":\"Мощная\",\"available\":false,\"requestId\":15}");

        assertThat(dto.getRequestId()).isEqualTo(15L);
        assertThat(dto.getAvailable()).isFalse();
    }

    @Test
    @DisplayName("JSON test 3: ItemRequestDto - пустое название, пустое описание и отсутствие available недопустимы")
    void itemRequestViolations() throws Exception {
        ItemRequestDto dto = itemRequestJson.parseObject("{\"name\":\"  \",\"description\":\"\"}");

        assertThat(invalidFields(dto)).containsExactlyInAnyOrder("name", "description", "available");
    }

    @Test
    @DisplayName("JSON test 4: название не больше 100 символов допустимо")
    void itemRequestNameLengthBoundary() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Мощная");
        dto.setAvailable(true);

        dto.setName("a".repeat(100));
        assertThat(invalidFields(dto)).isEmpty();

        dto.setName("a".repeat(101));
        assertThat(invalidFields(dto)).containsExactly("name");
    }

    @Test
    @DisplayName("JSON test 5: частичный JSON обновления оставляет непереданные поля равными null")
    void updateItemPartialJson() throws Exception {
        UpdateItemRequestDto dto = updateItemJson.parseObject("{\"available\":false}");

        assertThat(dto.getName()).isNull();
        assertThat(dto.getDescription()).isNull();
        assertThat(dto.getAvailable()).isFalse();
        assertThat(invalidFields(dto)).isEmpty();
    }

    @Test
    @DisplayName("JSON test 6: пробельное имя нарушает @Pattern, непустое допустимо")
    void updateItemBlankName() throws Exception {
        assertThat(invalidFields(updateItemJson.parseObject("{\"name\":\"   \"}"))).containsExactly("name");
        assertThat(invalidFields(updateItemJson.parseObject("{\"name\":\" дрель \"}"))).isEmpty();
    }

    @Test
    @DisplayName("JSON test 7: имя длиннее 100 символов нарушает @Size")
    void updateItemLongName() throws Exception {
        UpdateItemRequestDto dto = updateItemJson.parseObject("{\"name\":\"" + "a".repeat(101) + "\"}");

        assertThat(invalidFields(dto)).containsExactly("name");
    }

    @Test
    @DisplayName("JSON test 8: CommentRequestDto - пустой текст недопустим")
    void commentRequestBlankText() throws Exception {
        assertThat(invalidFields(commentRequestJson.parseObject("{\"text\":\"\"}"))).containsExactly("text");
        assertThat(invalidFields(commentRequestJson.parseObject("{\"text\":\"   \"}"))).containsExactly("text");
        assertThat(invalidFields(commentRequestJson.parseObject("{}"))).containsExactly("text");
    }

    @Test
    @DisplayName("JSON test 9: текст комментария не более 2000 символов допустимо")
    void commentRequestLengthBoundary() {
        CommentRequestDto dto = new CommentRequestDto();

        dto.setText("x".repeat(2000));
        assertThat(invalidFields(dto)).isEmpty();

        dto.setText("x".repeat(2001));
        assertThat(invalidFields(dto)).containsExactly("text");
    }

    @Test
    @DisplayName("JSON test 10: CommentResponseDto сериализует created в ISO-8601 и authorName")
    void commentResponseSerialize() throws Exception {
        CommentResponseDto dto = new CommentResponseDto(
                1L, "Супер", "Dominica", LocalDateTime.of(2030, 5, 5, 12, 0, 0));

        JsonContent<CommentResponseDto> result = commentResponseJson.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Супер");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("Dominica");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2030-05-05T12:00:00");
    }

    @Test
    @DisplayName("JSON test 11: ItemResponseDto сериализует last/next бронирование и комментарии")
    void itemResponseSerialize() throws Exception {
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 10, 0, 0);
        ItemResponseDto dto = new ItemResponseDto();
        dto.setId(3L);
        dto.setName("Дрель");
        dto.setDescription("Мощная");
        dto.setAvailable(true);
        dto.setLastBooking(new BookingShortDto(start, start.plusDays(1)));
        dto.setComments(List.of(new CommentResponseDto(1L, "Супер", "Dominica", start)));

        JsonContent<ItemResponseDto> result = itemResponseJson.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.lastBooking.start").isEqualTo("2030-01-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.end").isEqualTo("2030-01-02T10:00:00");
        assertThat(result).hasEmptyJsonPathValue("$.nextBooking");
        assertThat(result).extractingJsonPathStringValue("$.comments[0].authorName").isEqualTo("Dominica");
    }
}
