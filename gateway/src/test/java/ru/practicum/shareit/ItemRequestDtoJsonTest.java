package ru.practicum.shareit;

import jakarta.validation.Validator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestDtoJsonTest {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Autowired
    private JacksonTester<ItemRequestDto> itemRequestRequestJson;

    @Test
    @DisplayName("JSON test 1: описание запроса не более 1000 символов")
    void itemRequestRequestDescriptionBoundary() throws Exception {
        ItemRequestDto ok = itemRequestRequestJson.parseObject(
                "{\"description\":\"" + "d".repeat(100) + "\"}");
        ItemRequestDto tooLong = itemRequestRequestJson.parseObject(
                "{\"description\":\"" + "d".repeat(1001) + "\"}");
        ItemRequestDto blank = itemRequestRequestJson.parseObject("{\"description\":\"   \"}");

        assertThat(VALIDATOR.validate(ok)).isEmpty();
        assertThat(VALIDATOR.validate(tooLong)).hasSize(1);
        assertThat(VALIDATOR.validate(blank)).hasSize(1);
    }
}
