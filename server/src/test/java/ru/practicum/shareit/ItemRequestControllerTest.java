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
import ru.practicum.shareit.item.dto.ItemShortForRequestDto;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    private static final Long NOT_EXIST_ID = Long.MAX_VALUE;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mvc;

    private final ItemRequestResponseDto responseDto = makeResponse(1L, "Нужна дрель");

    private ItemRequestResponseDto makeResponse(long id, String description) {
        return new ItemRequestResponseDto(
                id,
                description,
                LocalDateTime.of(2027, 3, 1, 9, 15, 30),
                List.of(new ItemShortForRequestDto(10L, "Дрель", 2L)));
    }

    private ItemRequestRequestDto request(String description) {
        ItemRequestRequestDto dto = new ItemRequestRequestDto();
        dto.setDescription(description);
        return dto;
    }

    @Test
    @DisplayName("Feature-1: POST /requests -> 201, дата в формате yyyy-MM-dd'T'HH:mm:ss")
    void addItemRequestReturns201() throws Exception {
        when(itemRequestService.addItemRequest(eq(1L), any())).thenReturn(responseDto);

        mvc.perform(post("/requests")
                        .header(USER_HEADER, 1)
                        .content(mapper.writeValueAsString(request("Нужна дрель")))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(responseDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(responseDto.getDescription())))
                .andExpect(jsonPath("$.created").value("2027-03-01T09:15:30"))
                .andExpect(jsonPath("$.items[0].id").value(10))
                .andExpect(jsonPath("$.items[0].ownerId").value(2));
    }

    @Test
    @DisplayName("Feature-2: POST /requests с дублирующим описанием -> 409")
    void addItemRequestDuplicateReturns409() throws Exception {
        when(itemRequestService.addItemRequest(eq(1L), any()))
                .thenThrow(new ConflictException("Пользователь уже создал этот запрос"));

        mvc.perform(post("/requests")
                        .header(USER_HEADER, 1)
                        .content(mapper.writeValueAsString(request("Нужна дрель")))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Конфликт данных"));
    }

    @Test
    @DisplayName("Feature-3: GET /requests -> 200 и свои запросы")
    void getUserRequestsReturnsList() throws Exception {
        when(itemRequestService.getUserRequests(1L))
                .thenReturn(List.of(makeResponse(2L, "Второй"), makeResponse(1L, "Первый")));

        mvc.perform(get("/requests")
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].description").value("Второй"))
                .andExpect(jsonPath("$[1].description").value("Первый"));
    }

    @Test
    @DisplayName("Feature-4 GET /requests от несуществующего пользователя -> 404")
    void getUserRequestsUserNotFoundReturns404() throws Exception {
        when(itemRequestService.getUserRequests(NOT_EXIST_ID))
                .thenThrow(new NotFoundException("Пользователь с id = " + NOT_EXIST_ID + " не найден"));

        mvc.perform(get("/requests")
                        .header(USER_HEADER, NOT_EXIST_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Feature-5: GET /requests/all -> 200 и чужие запросы")
    void getAllRequestsReturnsList() throws Exception {
        when(itemRequestService.getAllRequests(1L, 0, 10))
                .thenReturn(List.of(makeResponse(5L, "Чужой запрос")));

        mvc.perform(get("/requests/all")
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(5));
    }

    @Test
    @DisplayName("Feature-6: GET /requests/all без чужих запросов -> пустой список")
    void getAllRequestsEmpty() throws Exception {
        when(itemRequestService.getAllRequests(1L, 0, 10)).thenReturn(List.of());

        mvc.perform(get("/requests/all")
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Feature-7: GET /requests/{id} -> 200")
    void getByRequestIdReturnsRequest() throws Exception {
        when(itemRequestService.getByRequestId(5L)).thenReturn(makeResponse(5L, "Нужна дрель"));

        mvc.perform(get("/requests/5")
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.items.length()").value(1));
    }

    @Test
    @DisplayName("Feature-8: GET /requests/{id} несуществующего -> 404")
    void getByRequestIdNotFoundReturns404() throws Exception {
        when(itemRequestService.getByRequestId(NOT_EXIST_ID)).thenThrow(new NotFoundException("Запрос не найден"));

        mvc.perform(get("/requests/" + NOT_EXIST_ID)
                        .header(USER_HEADER, 1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.description").value("Запрос не найден"));
    }
}
