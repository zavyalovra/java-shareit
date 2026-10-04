package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.user.dto.UserCreateDto;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserDtoJsonTest {
    private static final Validator VALIDATOR = Validation
            .buildDefaultValidatorFactory()
            .getValidator();

    @Autowired
    private JacksonTester<UserCreateDto> userRequestJson;

    @Test
    @DisplayName("Email: пустые и неверные адреса отклоняются, правильные принимаются")
    void emailValidation() {
        // 1. Защита по аннотации @NotBlank
        List<String> emptyEmails = Arrays.asList(
                null,
                "",
                "   "
        );

        for (String email : emptyEmails) {
            UserCreateDto dto = new UserCreateDto("Monica", email);
            Set<ConstraintViolation<UserCreateDto>> violations = VALIDATOR.validate(dto);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Email не может быть пустым");
        }

        // 2. Непустые некорректные адреса: их ловит @Email
        List<String> badEmails = Arrays.asList(
                "monica-beluchi.com",
                "monica@",
                "@beluchi.com",
                "monica@@beluchi.com",
                "mon ica@beluchi.com",
                "mon,ica@beluchi.com",
                "monica@bel uchi.com",
                "monica@bel,uchi.com"
        );

        for (String email : badEmails) {
            UserCreateDto dto = new UserCreateDto("Monica", email);
            Set<ConstraintViolation<UserCreateDto>> violations = VALIDATOR.validate(dto);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Некорректный формат email");
        }

        // 3. Корректные email
        List<String> correctEmails = Arrays.asList(
                "monica@beluchi.com",
                "monica.b@beluchi.com",
                "MONICA@BELUCHI.COM"
        );

        for (String email : correctEmails) {
            UserCreateDto dto = new UserCreateDto("Monica", email);
            Set<ConstraintViolation<UserCreateDto>> violations = VALIDATOR.validate(dto);
            assertThat(violations).isEmpty();
        }
    }
}
