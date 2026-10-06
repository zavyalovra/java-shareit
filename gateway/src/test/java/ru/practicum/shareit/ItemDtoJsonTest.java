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
import ru.practicum.shareit.item.dto.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

@JsonTest
public class ItemDtoJsonTest {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Autowired
    private JacksonTester<ItemCreateDto> itemRequestJson;

    @Autowired
    private JacksonTester<ItemUpdateDto> updateItemJson;

    @Autowired
    private JacksonTester<CommentRequestDto> commentRequestJson;

    private <T> List<String> invalidFields(T dto) {
        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(dto);
        return violations.stream()
                .map(v -> v.getPropertyPath().toString()).distinct()
                .toList();
    }

    @Test
    @DisplayName("JSON test 1: ItemRequestDto без requestId десериализуется, requestId = null")
    void itemRequestOptionalRequestId() throws Exception {
        ItemCreateDto dto = itemRequestJson.parseObject(
                "{\"name\":\"Дрель\",\"description\":\"Мощная\",\"available\":true}");

        Assertions.assertThat(dto.getName()).isEqualTo("Дрель");
        Assertions.assertThat(dto.getAvailable()).isTrue();
        Assertions.assertThat(dto.getRequestId()).isNull();
        assertThat(invalidFields(dto)).isEmpty();
    }

    @Test
    @DisplayName("JSON test 2: ItemRequestDto с requestId десериализуется")
    void itemRequestWithRequestId() throws Exception {
        ItemCreateDto dto = itemRequestJson.parseObject(
                "{\"name\":\"Дрель\",\"description\":\"Мощная\",\"available\":false,\"requestId\":15}");

        Assertions.assertThat(dto.getRequestId()).isEqualTo(15L);
        Assertions.assertThat(dto.getAvailable()).isFalse();
    }

    @Test
    @DisplayName("JSON test 3: ItemRequestDto - пустое название, пустое описание и отсутствие available недопустимы")
    void itemRequestViolations() throws Exception {
        ItemCreateDto dto = itemRequestJson.parseObject("{\"name\":\"  \",\"description\":\"\"}");

        assertThat(invalidFields(dto)).containsExactlyInAnyOrder("name", "description", "available");
    }

    @Test
    @DisplayName("JSON test 4: название не больше 100 символов допустимо")
    void itemRequestNameLengthBoundary() {
        ItemCreateDto dto = new ItemCreateDto();
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
        ItemUpdateDto dto = updateItemJson.parseObject("{\"available\":false}");

        Assertions.assertThat(dto.getName()).isNull();
        Assertions.assertThat(dto.getDescription()).isNull();
        Assertions.assertThat(dto.getAvailable()).isFalse();
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
        ItemUpdateDto dto = updateItemJson.parseObject("{\"name\":\"" + "a".repeat(101) + "\"}");

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
}
