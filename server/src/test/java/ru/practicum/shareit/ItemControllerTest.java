package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemService itemService;

    @Autowired
    private MockMvc mvc;

    private final ItemShortResponseDto responseDto = new ItemShortResponseDto(
            10L,
            "Дрель",
            "Берёт бетон",
            true);

    private ItemRequestDto request(String name, String description, Boolean available) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(available);
        return dto;
    }

    private CommentRequestDto commentRequest(String text) {
        CommentRequestDto dto = new CommentRequestDto();
        dto.setText(text);
        return dto;
    }

    @Test
    @DisplayName("Feature-1: GET /items -> 200 и вещи владельца с бронированиями и комментариями")
    void findOwnerItemsReturnsList() throws Exception {
        LocalDateTime start = LocalDateTime.of(2027, 1, 1, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2027, 1, 2, 10, 0, 0);
        ItemResponseDto dto = new ItemResponseDto();
        dto.setId(1L);
        dto.setName("Дрель");
        dto.setDescription("Берёт бетон");
        dto.setAvailable(true);
        dto.setLastBooking(new BookingShortDto(start, end));
        dto.setNextBooking(null);
        dto.setComments(List.of(new CommentResponseDto(5L, "Отлично", "Dominica", start)));
        when(itemService.getByOwnerId(1L)).thenReturn(List.of(dto));

        mvc.perform(get("/items")
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"))
                .andExpect(jsonPath("$[0].lastBooking.start").value("2027-01-01T10:00:00"))
                .andExpect(jsonPath("$[0].lastBooking.end").value("2027-01-02T10:00:00"))
                .andExpect(jsonPath("$[0].comments[0].authorName").value("Dominica"));
    }

    @Test
    @DisplayName("Feature-2: GET /items/{id} -> 200")
    void findItemByIdReturnsItem() throws Exception {
        ItemResponseDto dto = new ItemResponseDto();
        dto.setId(7L);
        dto.setName("Пила");
        dto.setDescription("Бензиновая громкая");
        dto.setAvailable(false);
        dto.setComments(List.of());
        when(itemService.getItemById(7L, 1L)).thenReturn(dto);

        mvc.perform(get("/items/7")
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.comments").isEmpty());
    }

    @Test
    @DisplayName("Feature-3: GET /items/{id} несуществующей вещи -> 404")
    void findItemByIdNotFoundReturns404() throws Exception {
        when(itemService.getItemById(99L, 1L))
                .thenThrow(new NotFoundException("Вещь с id=99 не найдена"));

        mvc.perform(get("/items/99")
                        .header(USER_HEADER, 1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Ресурс не найден"))
                .andExpect(jsonPath("$.description").value("Вещь с id=99 не найдена"));
    }

    @Test
    @DisplayName("Feature-4: POST /items -> 200 и краткое представление вещи")
    void createItemReturnsShortDto() throws Exception {
        when(itemService.createItem(eq(1L), any()))
                .thenReturn(new ItemShortResponseDto(responseDto.getId(), responseDto.getName(),
                        responseDto.getDescription(), responseDto.getAvailable()));

        mvc.perform(post("/items")
                        .header(USER_HEADER, 1)
                        .content(mapper.writeValueAsString(request("Дрель", "Мощная дрель", true)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(responseDto.getName())))
                .andExpect(jsonPath("$.available", is(responseDto.getAvailable())));
    }

    @Test
    @DisplayName("Feature-5: POST /items с пустым названием -> 400")
    void createItemBlankNameReturns400() throws Exception {
        mvc.perform(post("/items")
                        .header(USER_HEADER, 1)
                        .content(mapper.writeValueAsString(request(" ", "Мощная дрель", true)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Название не может быть пустым")));

        verify(itemService, never()).createItem(any(), any());
    }

    @Test
    @DisplayName("Feature-6: POST /items с названием длиннее 100 символов -> 400")
    void createItemLongNameReturns400() throws Exception {
        mvc.perform(post("/items")
                        .header(USER_HEADER, 1)
                        .content(mapper.writeValueAsString(request("a".repeat(101), "Мощная дрель", true)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Максимальная длина названия")));
    }

    @Test
    @DisplayName("Feature-7: POST /items без description -> 400")
    void createItemNoDescriptionReturns400() throws Exception {
        mvc.perform(post("/items")
                        .header(USER_HEADER, 1)
                        .content(mapper.writeValueAsString(request("Дрель", null, true)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).createItem(any(), any());
    }

    @Test
    @DisplayName("Feature-8: POST /items без available -> 400")
    void createItemNoAvailableReturns400() throws Exception {
        mvc.perform(post("/items")
                        .header(USER_HEADER, 1)
                        .content(mapper.writeValueAsString(request("Дрель", "Берёт бетон", null)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).createItem(any(), any());
    }

    @Test
    @DisplayName("Feature-9: POST /items от несуществующего пользователя -> 404")
    void createItemUserNotFoundReturns404() throws Exception {
        when(itemService.createItem(eq(99L), any()))
                .thenThrow(new NotFoundException("Пользователь с id = 99 не найден"));

        mvc.perform(post("/items")
                        .header(USER_HEADER, 99)
                        .content(mapper.writeValueAsString(request("Дрель", "Берёт бетон", true)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Feature-10: PATCH /items/{id} -> 200 и обновлённая вещь")
    void updateItemReturnsUpdated() throws Exception {
        when(itemService.updateItem(eq(10L), eq(1L), any()))
                .thenReturn(new ItemShortResponseDto(responseDto.getId(), "Новая дрель",
                        responseDto.getDescription(), responseDto.getAvailable()));

        mvc.perform(patch("/items/10")
                        .header(USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Новая дрель\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новая дрель"));
    }

    @Test
    @DisplayName("Feature-11: PATCH /items/{id} не владельцем -> 403")
    void updateItemNotOwnerReturns403() throws Exception {
        when(itemService.updateItem(eq(10L), eq(2L), any()))
                .thenThrow(new ForbiddenException("Пользователь не владелец вещи"));

        mvc.perform(patch("/items/10")
                        .header(USER_HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"available\":false}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Ошибка авторизации"));
    }

    @Test
    @DisplayName("Feature-12: PATCH /items/{id} с пустым именем -> 400")
    void updateItemBlankNameReturns400() throws Exception {
        mvc.perform(patch("/items/10")
                        .header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Имя не может быть пустым")));

        verify(itemService, never()).updateItem(any(), any(), any());
    }

    @Test
    @DisplayName("Feature-13: GET /items/search?text= -> 200 и найденные вещи")
    void searchItemsReturnsList() throws Exception {
        when(itemService.searchItems("дрель")).thenReturn(List.of(responseDto));

        mvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    @DisplayName("Feature-14: GET /items/search с пустым текстом -> 200 и пустой список")
    void searchItemsBlankTextReturnsEmptyList() throws Exception {
        when(itemService.searchItems("")).thenReturn(List.of());

        mvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Feature-15: POST /items/{id}/comment -> 200 и комментарий")
    void createCommentReturnsComment() throws Exception {
        LocalDateTime created = LocalDateTime.of(2030, 5, 5, 12, 0, 0);
        when(itemService.createComment(eq(10L), eq(2L), any()))
                .thenReturn(new CommentResponseDto(1L, "Супер", "Dominica", created));

        mvc.perform(post("/items/10/comment")
                        .header(USER_HEADER, 2)
                        .content(mapper.writeValueAsString(commentRequest("Супер")))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Супер"))
                .andExpect(jsonPath("$.authorName").value("Dominica"))
                .andExpect(jsonPath("$.created").value("2030-05-05T12:00:00"));
    }

    @Test
    @DisplayName("Feature-16: POST /items/{id}/comment без завершённой брони -> 400")
    void createCommentNoBookingReturns400() throws Exception {
        when(itemService.createComment(eq(10L), eq(2L), any()))
                .thenThrow(new ValidationException("Пользователь не найден в истории брони этой вещи"));

        mvc.perform(post("/items/10/comment")
                        .header(USER_HEADER, 2)
                        .content(mapper.writeValueAsString(commentRequest("Супер")))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"));
    }

    @Test
    @DisplayName("Feature-17: POST /items/{id}/comment с пустым текстом -> 400")
    void createCommentBlankTextReturns400() throws Exception {
        mvc.perform(post("/items/10/comment")
                        .header(USER_HEADER, 2)
                        .content(mapper.writeValueAsString(commentRequest("  ")))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("Содержание не может быть пустым")));

        verify(itemService, never()).createComment(any(), any(), any());
    }

    @Test
    @DisplayName("Feature-18: POST /items/{id}/comment с текстом длиннее 2000 символов -> 400")
    void createCommentTooLongTextReturns400() throws Exception {
        mvc.perform(post("/items/10/comment")
                        .header(USER_HEADER, 2)
                        .content(mapper.writeValueAsString(commentRequest("x".repeat(2001))))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(containsString("2000")));
    }
}
