package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemShortResponseDto;
import ru.practicum.shareit.user.UserResponseDto;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    private final BookingResponseDto responseDto = makeResponse(1L, BookingStatus.WAITING);

    private BookingResponseDto makeResponse(long id, BookingStatus status) {
        return new BookingResponseDto(
                id,
                LocalDateTime.of(2027, 1, 1, 10, 0, 0),
                LocalDateTime.of(2027, 1, 2, 10, 0, 0),
                status,
                new UserResponseDto(2L, "Dominica", "dominica@beluchi.com"),
                new ItemShortResponseDto(5L, "Дрель", "Мощная дрель", true));
    }

    private BookingRequestDto request(Long itemId, LocalDateTime start, LocalDateTime end) {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(itemId);
        dto.setStart(start);
        dto.setEnd(end);
        return dto;
    }

    @Test
    @DisplayName("Feature-1: POST /bookings -> 200 и бронирование со статусом WAITING")
    void createBookingReturnsWaitingBooking() throws Exception {
        when(bookingService.createBooking(eq(2L), any())).thenReturn(responseDto);
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        mvc.perform(post("/bookings")
                        .header(USER_HEADER, 2)
                        .content(mapper.writeValueAsString(request(5L, start, start.plusDays(1))))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseDto.getId()), Long.class))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.start").value("2027-01-01T10:00:00"))
                .andExpect(jsonPath("$.booker.id").value(2))
                .andExpect(jsonPath("$.item.name").value("Дрель"));
    }

    @Test
    @DisplayName("Feature-2: POST /bookings с датой окончания в прошлом -> 400")
    void createBookingEndInPastReturns400() throws Exception {
        LocalDateTime start = LocalDateTime.now().minusDays(3);

        mvc.perform(post("/bookings")
                        .header(USER_HEADER, 2)
                        .content(mapper.writeValueAsString(request(5L, start, start.plusDays(1))))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).createBooking(any(), any());
    }

    @Test
    @DisplayName("Feature-3: POST /bookings без itemId -> 400")
    void createBookingNoItemIdReturns400() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        mvc.perform(post("/bookings")
                        .header(USER_HEADER, 2)
                        .content(mapper.writeValueAsString(request(null, start, start.plusDays(1))))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).createBooking(any(), any());
    }

    @Test
    @DisplayName("Feature-4: POST /bookings без дат -> 400")
    void createBookingNoDatesReturns400() throws Exception {
        mvc.perform(post("/bookings")
                        .header(USER_HEADER, 2)
                        .content(mapper.writeValueAsString(request(5L, null, null)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).createBooking(any(), any());
    }

    @Test
    @DisplayName("Feature-5: POST /bookings -> 400 при ошибке бизнес-валидации")
    void createBookingBusinessValidationErrorReturns400() throws Exception {
        when(bookingService.createBooking(eq(2L), any()))
                .thenThrow(new ValidationException("Вещь для бронирования недоступна"));
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        mvc.perform(post("/bookings")
                        .header(USER_HEADER, 2)
                        .content(mapper.writeValueAsString(request(5L, start, start.plusDays(1))))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.description").value("Вещь для бронирования недоступна"));
    }

    @Test
    @DisplayName("Feature-6: POST /bookings несуществующей вещи -> 404")
    void createBookingItemNotFoundReturns404() throws Exception {
        when(bookingService.createBooking(eq(2L), any()))
                .thenThrow(new NotFoundException("Вещь с id=5 не найдена"));
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        mvc.perform(post("/bookings")
                        .header(USER_HEADER, 2)
                        .content(mapper.writeValueAsString(request(5L, start, start.plusDays(1))))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Feature-7: PATCH /bookings/{id}?approved=true -> 200 и статус APPROVED")
    void approveBookingApproved() throws Exception {
        when(bookingService.approveBooking(1L, 1L, true)).thenReturn(makeResponse(1L, BookingStatus.APPROVED));

        mvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 1)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("Feature-8: PATCH /bookings/{id}?approved=false -> 200 и статус REJECTED")
    void approveBookingRejected() throws Exception {
        when(bookingService.approveBooking(1L, 1L, false)).thenReturn(makeResponse(1L, BookingStatus.REJECTED));

        mvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 1)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @DisplayName("Feature-9: PATCH /bookings/{id} без параметра approved -> approved=false по умолчанию")
    void approveBookingDefaultsToFalse() throws Exception {
        when(bookingService.approveBooking(1L, 1L, false)).thenReturn(makeResponse(1L, BookingStatus.REJECTED));

        mvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).approveBooking(1L, 1L, false);
    }

    @Test
    @DisplayName("Feature-10: PATCH /bookings/{id} не владельцем -> 403")
    void approveBookingNotOwnerReturns403() throws Exception {
        when(bookingService.approveBooking(3L, 1L, true))
                .thenThrow(new ForbiddenException("Пользователь не является владельцем вещи"));

        mvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 3)
                        .param("approved", "true"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Ошибка авторизации"));
    }

    @Test
    @DisplayName("Feature-11: PATCH /bookings/{id} повторно -> 400")
    void approveBookingAlreadyDecidedReturns400() throws Exception {
        when(bookingService.approveBooking(1L, 1L, true))
                .thenThrow(new ValidationException("Это бронирование недоступно"));

        mvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 1)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Feature-12: GET /bookings/{id} -> 200")
    void getBookingReturnsBooking() throws Exception {
        when(bookingService.getBooking(2L, 1L)).thenReturn(responseDto);

        mvc.perform(get("/bookings/1")
                        .header(USER_HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id").value(5));
    }

    @Test
    @DisplayName("Feature-13: GET /bookings/{id} постороннему пользователю -> 403")
    void getBookingStrangerReturns403() throws Exception {
        when(bookingService.getBooking(9L, 1L))
                .thenThrow(new ForbiddenException("Доступ для пользователя 9 запрещен"));

        mvc.perform(get("/bookings/1")
                        .header(USER_HEADER, 9))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Feature-14: GET /bookings/{id} несуществующей -> 404")
    void getBookingNotFoundReturns404() throws Exception {
        when(bookingService.getBooking(2L, 99L))
                .thenThrow(new NotFoundException("Бронирование с id = 99 не найдено"));

        mvc.perform(get("/bookings/99")
                        .header(USER_HEADER, 2))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Feature-15: GET /bookings без state -> state=ALL по умолчанию")
    void getBookingsForCurrentUserDefaultStateAll() throws Exception {
        when(bookingService.getBookingsForCurrentUser(2L, "ALL"))
                .thenReturn(List.of(makeResponse(1L, BookingStatus.WAITING), makeResponse(2L, BookingStatus.APPROVED)));

        mvc.perform(get("/bookings")
                        .header(USER_HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].status").value("APPROVED"));

        verify(bookingService, times(1)).getBookingsForCurrentUser(2L, "ALL");
    }

    @Test
    @DisplayName("Feature-16: GET /bookings?state=FUTURE передаёт state в сервис")
    void getBookingsForCurrentUserPassesState() throws Exception {
        when(bookingService.getBookingsForCurrentUser(2L, "FUTURE")).thenReturn(List.of());

        mvc.perform(get("/bookings")
                        .header(USER_HEADER, 2)
                        .param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(bookingService, times(1)).getBookingsForCurrentUser(2L, "FUTURE");
    }

    @Test
    @DisplayName("Feature-17: GET /bookings?state=UNKNOWN -> 400")
    void getBookingsForCurrentUserUnknownStateReturns400() throws Exception {
        when(bookingService.getBookingsForCurrentUser(2L, "UNKNOWN"))
                .thenThrow(new ValidationException("Запрос не поддерживается"));

        mvc.perform(get("/bookings")
                        .header(USER_HEADER, 2)
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Запрос не поддерживается"));
    }

    @Test
    @DisplayName("Feature-18: GET /bookings/owner без state -> state=ALL по умолчанию")
    void getOwnersBookingsDefaultStateAll() throws Exception {
        when(bookingService.getOwnersBookings(1L, "ALL"))
                .thenReturn(List.of(makeResponse(1L, BookingStatus.WAITING)));

        mvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].booker.name").value("Dominica"));

        verify(bookingService, times(1)).getOwnersBookings(1L, "ALL");
    }

    @Test
    @DisplayName("Feature-19: GET /bookings/owner?state=PAST передаёт state в сервис")
    void getOwnersBookingsPassesState() throws Exception {
        when(bookingService.getOwnersBookings(1L, "PAST")).thenReturn(List.of());

        mvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, 1)
                        .param("state", "PAST"))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).getOwnersBookings(1L, "PAST");
    }

    @Test
    @DisplayName("Feature-20: GET /bookings/owner для несуществующего пользователя -> 404")
    void getOwnersBookingsUserNotFoundReturns404() throws Exception {
        when(bookingService.getOwnersBookings(99L, "ALL"))
                .thenThrow(new NotFoundException("Пользователь с id = 99 не найден"));

        mvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, 99))
                .andExpect(status().isNotFound());
    }
}
