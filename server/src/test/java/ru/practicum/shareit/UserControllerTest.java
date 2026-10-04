package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    private static final Long NOT_EXIST_ID = Long.MAX_VALUE;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    UserService userService;

    @Autowired
    private MockMvc mvc;

    private final UserResponseDto responseDto = new UserResponseDto(
            1L,
            "Monica",
            "monica@beluchi.com");

    private UserRequestDto request(String name, String email) {
        UserRequestDto dto = new UserRequestDto();
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }

    @Test
    @DisplayName("Feature-1: POST /users -> 201 и тело пользователя")
    void createUserReturn201() throws Exception {
        when(userService.createUser(any()))
                .thenReturn(new UserResponseDto(responseDto.getId(), responseDto.getName(), responseDto.getEmail()));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(responseDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(responseDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(responseDto.getName())))
                .andExpect(jsonPath("$.email", is(responseDto.getEmail())));
    }

    @Test
    @DisplayName("Feature-2: POST /users дубликат email -> 409")
    void createUserConflictReturns409() throws Exception {
        when(userService.createUser(any()))
                .thenThrow(new ConflictException("Пользователь с email=monica@beluchi.com уже существует"));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(request(responseDto.getName(), responseDto.getEmail())))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Конфликт данных"))
                .andExpect(jsonPath("$.description").value(containsString("monica@beluchi.com")));
    }

    @Test
    @DisplayName("Feature-3: GET /users -> 200 и список")
    void findAllUsersReturnsList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(
                new UserResponseDto(1L, "Monica", "monica@beluchi.com"),
                new UserResponseDto(2L, "Dominica", "dominica@beluchi.com")));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Feature-4: GET /users/{id} -> 200")
    void findUserByIdReturnsUser() throws Exception {
        when(userService.getUserById(1L))
                .thenReturn(new UserResponseDto(responseDto.getId(), responseDto.getName(), responseDto.getEmail()));

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(responseDto.getName())))
                .andExpect(jsonPath("$.email", is(responseDto.getEmail())));
    }

    @Test
    @DisplayName("Feature-5: GET /users/{id} несуществующего -> 404")
    void findUserByIdNotFoundReturns404() throws Exception {
        when(userService.getUserById(NOT_EXIST_ID))
                .thenThrow(new NotFoundException("Пользователь с id = " + NOT_EXIST_ID + " не найден"));

        mvc.perform(get("/users/" + NOT_EXIST_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Ресурс не найден"))
                .andExpect(jsonPath("$.description").value("Пользователь с id = " + NOT_EXIST_ID + " не найден"));
    }

    @Test
    @DisplayName("Feature-6: PATCH /users/{id} -> 200 и обновлённый пользователь")
    void updateUserReturnsUpdated() throws Exception {
        when(userService.updateUser(eq(1L), any()))
                .thenReturn(new UserResponseDto(responseDto.getId(), "New name", responseDto.getEmail()));

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New name"));
    }

    @Test
    @DisplayName("Feature-7: PATCH /users/{id} на занятый email -> 409")
    void updateUserConflictReturns409() throws Exception {
        when(userService.updateUser(anyLong(), any()))
                .thenThrow(new ConflictException("Пользователь с email=monica@beluchi.com уже существует"));

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"monica@beluchi.com\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Feature-8: DELETE /users/{id} -> 204")
    void deleteUserReturns204() throws Exception {
        mvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }
}
