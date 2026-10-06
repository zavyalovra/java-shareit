package ru.practicum.shareit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;

public class BookingServiceImplTest extends IntegrationTest {
    private static final Long NOT_EXIST_ID = Long.MAX_VALUE;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    private Long ownerId;
    private Long bookerId;
    private Long pastId;
    private Long currentId;
    private Long futureId;
    private Long waitingId;
    private Long rejectedId;

    private void prepareBookings() {
        LocalDateTime now = LocalDateTime.now();
        User owner = persistUser("Monica", "monica@beluchi.com");
        User booker = persistUser("Dominica", "dominica@beluchi.com");
        User other = persistUser("Veronica", "veronica@beluchi.com");
        Item item = persistItem(owner, "Дрель", "Мощная дрель", true);
        Item otherItem = persistItem(other, "Пила", "Бензопила", true);

        Booking past = persistBooking(item, booker, now.minusDays(5), now.minusDays(4), BookingStatus.APPROVED);
        Booking current = persistBooking(item, booker, now.minusDays(1), now.plusDays(1), BookingStatus.APPROVED);
        Booking future = persistBooking(item, booker, now.plusDays(3), now.plusDays(4), BookingStatus.APPROVED);
        Booking waiting = persistBooking(item, booker, now.plusDays(6), now.plusDays(7), BookingStatus.WAITING);
        Booking rejected = persistBooking(item, booker, now.plusDays(8), now.plusDays(9), BookingStatus.REJECTED);
        persistBooking(otherItem, other, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING);
        flushAndClear();

        ownerId = owner.getId();
        bookerId = booker.getId();
        pastId = past.getId();
        currentId = current.getId();
        futureId = future.getId();
        waitingId = waiting.getId();
        rejectedId = rejected.getId();
    }

    private List<Long> ids(List<BookingResponseDto> bookings) {
        return bookings.stream().map(BookingResponseDto::getId).toList();
    }

    @Test
    @DisplayName("IntegrationTest-1: createBooking сохраняет бронирование со статусом WAITING")
    void createBookingPersistsWaitingBooking() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime end = start.plusDays(1).withNano(0);
        User owner = persistUser("Monica", "monica@beluchi.com");
        User booker = persistUser("Dominica", "dominica@beluchi.com");
        Item item = persistItem(owner, "Дрель", "Мощная дрель", true);
        flushAndClear();

        BookingResponseDto created = bookingService.createBooking(booker.getId(),
                makeBookingDto(item.getId(), start, end));
        flushAndClear();

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(created.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(created.getItem().getId()).isEqualTo(item.getId());
        Booking fromDb = bookingRepository.findById(created.getId()).orElseThrow();
        assertThat(fromDb.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(fromDb.getStart()).isEqualTo(start);
        assertThat(fromDb.getEnd()).isEqualTo(end);
        assertThat(fromDb.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(fromDb.getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    @DisplayName("IntegrationTest-2: createBooking недоступной вещи -> ValidationException")
    void createBookingUnavailableItemThrowsValidation() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        User booker = persistUser("Dominica", "dominica@beluchi.com");
        Item item = persistItem(owner, "Дрель", "Мощная дрель", false);
        flushAndClear();
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> bookingService.createBooking(booker.getId(),
                makeBookingDto(item.getId(), start, start.plusDays(1))))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("недоступна");
        assertThat(bookingRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("IntegrationTest-3: createBooking с end <= start -> ValidationException")
    void createBookingEndNotAfterStartThrowsValidation() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        User booker = persistUser("Dominica", "dominica@beluchi.com");
        Item item = persistItem(owner, "Дрель", "Мощная дрель", true);
        flushAndClear();
        LocalDateTime start = LocalDateTime.now().plusDays(2);

        assertThatThrownBy(() -> bookingService.createBooking(booker.getId(),
                makeBookingDto(item.getId(), start, start.minusHours(1))))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> bookingService.createBooking(booker.getId(),
                makeBookingDto(item.getId(), start, start)))
                .isInstanceOf(ValidationException.class);
        assertThat(bookingRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("IntegrationTest-4: владелец не может бронировать свою вещь -> ValidationException")
    void createBookingOwnerBooksOwnItemThrowsValidation() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        Item item = persistItem(owner, "Дрель", "Мощная дрель", true);
        flushAndClear();
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> bookingService.createBooking(owner.getId(),
                makeBookingDto(item.getId(), start, start.plusDays(1))))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("IntegrationTest-5: createBooking с несуществующими пользователем или вещью -> NotFoundException")
    void createBookingNotFound() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        User booker = persistUser("Dominica", "dominica@beluchi.com");
        Item item = persistItem(owner, "Дрель", "Мощная дрель", true);
        flushAndClear();
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> bookingService.createBooking(NOT_EXIST_ID,
                makeBookingDto(item.getId(), start, start.plusDays(1))))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> bookingService.createBooking(booker.getId(),
                makeBookingDto(NOT_EXIST_ID, start, start.plusDays(1))))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("IntegrationTest-6: approveBooking(true) переводит бронь в APPROVED и сохраняет в БД")
    void approveBookingApproves() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        User booker = persistUser("Dominica", "dominica@beluchi.com");
        Item item = persistItem(owner, "Дрель", "Мощная дрель", true);
        Booking booking = persistBooking(item, booker, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), BookingStatus.WAITING);
        flushAndClear();

        BookingResponseDto result = bookingService.approveBooking(owner.getId(), booking.getId(), true);
        flushAndClear();

        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(bookingRepository.findById(booking.getId()).orElseThrow().getStatus())
                .isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    @DisplayName("IntegrationTest-7: approveBooking(false) переводит бронь в REJECTED")
    void approveBookingRejects() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        User booker = persistUser("Dominica", "dominica@beluchi.com");
        Item item = persistItem(owner, "Дрель", "Мощная дрель", true);
        Booking booking = persistBooking(item, booker, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), BookingStatus.WAITING);
        flushAndClear();

        BookingResponseDto result = bookingService.approveBooking(owner.getId(), booking.getId(), false);
        flushAndClear();

        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(bookingRepository.findById(booking.getId()).orElseThrow().getStatus())
                .isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    @DisplayName("IntegrationTest-8: approveBooking не владельцем -> ForbiddenException, статус не меняется")
    void approveBookingNotOwnerThrowsForbidden() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        User booker = persistUser("Dominica", "dominica@beluchi.com");
        Item item = persistItem(owner, "Дрель", "Мощная дрель", true);
        Booking booking = persistBooking(item, booker, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), BookingStatus.WAITING);
        flushAndClear();

        assertThatThrownBy(() -> bookingService.approveBooking(booker.getId(), booking.getId(), true))
                .isInstanceOf(ForbiddenException.class);
        flushAndClear();
        assertThat(bookingRepository.findById(booking.getId()).orElseThrow().getStatus())
                .isEqualTo(BookingStatus.WAITING);
    }

    @Test
    @DisplayName("IntegrationTest-9: повторное решение по брони -> ValidationException")
    void approveBookingAlreadyDecidedThrowsValidation() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        User booker = persistUser("Dominica", "dominica@beluchi.com");
        Item item = persistItem(owner, "Дрель", "Мощная дрель", true);
        Booking booking = persistBooking(item, booker, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), BookingStatus.APPROVED);
        flushAndClear();

        assertThatThrownBy(() -> bookingService.approveBooking(owner.getId(), booking.getId(), false))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("IntegrationTest-10: approveBooking несуществующей брони -> NotFoundException")
    void approveBookingNotFound() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        flushAndClear();

        assertThatThrownBy(() -> bookingService.approveBooking(owner.getId(), NOT_EXIST_ID, true))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("IntegrationTest-11: getBooking доступен автору брони и владельцу вещи")
    void getBookingBookerAndOwnerHaveAccess() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        User booker = persistUser("Dominica", "dominica@beluchi.com");
        Item item = persistItem(owner, "Дрель", "Мощная дрель", true);
        Booking booking = persistBooking(item, booker, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), BookingStatus.WAITING);
        flushAndClear();

        BookingResponseDto forBooker = bookingService.getBooking(booker.getId(), booking.getId());
        BookingResponseDto forOwner = bookingService.getBooking(owner.getId(), booking.getId());

        assertThat(forBooker.getId()).isEqualTo(booking.getId());
        assertThat(forBooker.getBooker().getName()).isEqualTo("Dominica");
        assertThat(forBooker.getItem().getName()).isEqualTo("Дрель");
        assertThat(forOwner.getId()).isEqualTo(booking.getId());
    }

    @Test
    @DisplayName("IntegrationTest-12: getBooking постороннему пользователю -> ForbiddenException")
    void getBookingStrangerThrowsForbidden() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        User booker = persistUser("Dominica", "dominica@beluchi.com");
        User stranger = persistUser("Veronica", "veronica@beluchi.com");
        Item item = persistItem(owner, "Дрель", "Мощная дрель", true);
        Booking booking = persistBooking(item, booker, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), BookingStatus.WAITING);
        flushAndClear();

        assertThatThrownBy(() -> bookingService.getBooking(stranger.getId(), booking.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("IntegrationTest-13: getBooking несуществующей брони или пользователя -> NotFoundException")
    void getBookingNotFound() {
        User user = persistUser("Monica", "monica@beluchi.com");
        flushAndClear();

        assertThatThrownBy(() -> bookingService.getBooking(user.getId(), NOT_EXIST_ID))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> bookingService.getBooking(NOT_EXIST_ID, 1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("IntegrationTest-14: getBookingsForCurrentUser фильтрует по state и сортирует по start убыванию")
    void getBookingsForCurrentUserFiltersByState() {
        prepareBookings();

        assertThat(ids(bookingService.getBookingsForCurrentUser(bookerId, "ALL")))
                .containsExactly(rejectedId, waitingId, futureId, currentId, pastId);
        assertThat(ids(bookingService.getBookingsForCurrentUser(bookerId, "CURRENT")))
                .containsExactly(currentId);
        assertThat(ids(bookingService.getBookingsForCurrentUser(bookerId, "PAST")))
                .containsExactly(pastId);
        assertThat(ids(bookingService.getBookingsForCurrentUser(bookerId, "FUTURE")))
                .containsExactly(rejectedId, waitingId, futureId);
        assertThat(ids(bookingService.getBookingsForCurrentUser(bookerId, "WAITING")))
                .containsExactly(waitingId);
        assertThat(ids(bookingService.getBookingsForCurrentUser(bookerId, "REJECTED")))
                .containsExactly(rejectedId);
    }

    @Test
    @DisplayName("IntegrationTest-15: state не чувствителен к регистру, у пользователя без броней список пуст")
    void getBookingsForCurrentUserCaseInsensitiveAndEmpty() {
        prepareBookings();

        assertThat(bookingService.getBookingsForCurrentUser(bookerId, "waiting")).hasSize(1);
        assertThat(bookingService.getBookingsForCurrentUser(ownerId, "ALL")).isEmpty();
    }

    @Test
    @DisplayName("IntegrationTest-16: getBookingsForCurrentUser с неизвестным state -> ValidationException")
    void getBookingsForCurrentUserUnknownStateThrowsValidation() {
        prepareBookings();

        assertThatThrownBy(() -> bookingService.getBookingsForCurrentUser(bookerId, "UNKNOWN"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Запрос не поддерживается");
    }

    @Test
    @DisplayName("IntegrationTest-17: getBookingsForCurrentUser несуществующего пользователя -> NotFoundException")
    void getBookingsForCurrentUserNotFound() {
        assertThatThrownBy(() -> bookingService.getBookingsForCurrentUser(NOT_EXIST_ID, "ALL"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("IntegrationTest-18: getOwnersBookings фильтрует по state и сортирует по start убыванию")
    void getOwnersBookingsFiltersByState() {
        prepareBookings();

        assertThat(ids(bookingService.getOwnersBookings(ownerId, "ALL")))
                .containsExactly(rejectedId, waitingId, futureId, currentId, pastId);
        assertThat(ids(bookingService.getOwnersBookings(ownerId, "CURRENT")))
                .containsExactly(currentId);
        assertThat(ids(bookingService.getOwnersBookings(ownerId, "PAST")))
                .containsExactly(pastId);
        assertThat(ids(bookingService.getOwnersBookings(ownerId, "FUTURE")))
                .containsExactly(rejectedId, waitingId, futureId);
        assertThat(ids(bookingService.getOwnersBookings(ownerId, "WAITING")))
                .containsExactly(waitingId);
        assertThat(ids(bookingService.getOwnersBookings(ownerId, "REJECTED")))
                .containsExactly(rejectedId);
    }

    @Test
    @DisplayName("IntegrationTest-19: getOwnersBookings у не владельца возвращает пустой список, неизвестный state -> 400")
    void getOwnersBookingsNonOwnerAndUnknownState() {
        prepareBookings();

        assertThat(bookingService.getOwnersBookings(bookerId, "ALL")).isEmpty();
        assertThatThrownBy(() -> bookingService.getOwnersBookings(ownerId, "BAD"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> bookingService.getOwnersBookings(NOT_EXIST_ID, "ALL"))
                .isInstanceOf(NotFoundException.class);
    }
}
