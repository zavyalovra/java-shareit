package ru.practicum.shareit;

import jakarta.validation.Validator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.UserRequestDto;
import ru.practicum.shareit.user.UserResponseDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserDtoJsonTest {
    private static final Validator VALIDATOR = Validation
            .buildDefaultValidatorFactory()
            .getValidator();

    @Autowired
    private JacksonTester<UserRequestDto> userRequestJson;

    @Autowired
    private JacksonTester<UserResponseDto> userResponseJson;

    private final String jsonString = "{\"name\":\"Monica\",\"email\":\"monica@beluchi.com\"}";

    @Test
    @DisplayName("JSON test 1: UserRequestDto десериализуется")
    void userRequest_valid() throws Exception {
        UserRequestDto dto = new UserRequestDto("Monica", "monica@beluchi.com");

        JsonContent<UserRequestDto> result = userRequestJson.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Monica");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("monica@beluchi.com");
    }

    @Test
    @DisplayName("JSON test 2: UserRequestDto читается из JSON")
    void userRequestDeserializesAndPassesValidation() throws Exception {
        UserRequestDto dto = userRequestJson.parseObject(jsonString);

        assertThat(dto.getName()).isEqualTo("Monica");
        assertThat(dto.getEmail()).isEqualTo("monica@beluchi.com");
    }

    @Test
    @DisplayName("JSON test 3: некорректный email нарушает @Email")
    void userRequestInvalidEmail() throws Exception {
        UserRequestDto dto = userRequestJson.parseObject("{\"name\":\"Monica\",\"email\":\"monica-beluchi.com\"}");

        assertThat(VALIDATOR.validate(dto)).isNotEmpty();
    }

    @Test
    @DisplayName("JSON test 4: UserResponseDto сериализуется с id, name, email")
    void userResponseSerialize() throws Exception {
        JsonContent<UserResponseDto> result = userResponseJson.write(
                new UserResponseDto(1L, "Monica", "monica@beluchi.com"));

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Monica");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("monica@beluchi.com");
    }
}
