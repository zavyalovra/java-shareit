package ru.practicum.shareit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRequestDto;

import java.time.LocalDateTime;

/**
 * Базовый класс интеграционных тестов: поднимается БД H2 test,
 * схема создается из schema.sql,
 * каждый тест выполняется в транзакции и откатывается
 * ~~~~~
 * Аннотация @PersistenceContext, а так же
 * необходимость очистки кэша БД flushAndClear() - это совет ИИ.
 */
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
abstract class IntegrationTest {

    @PersistenceContext
    protected EntityManager em;

    protected void flushAndClear() {
        em.flush();
        em.clear();
    }

    protected UserRequestDto makeUserDto(String name, String email) {
        UserRequestDto dto = new UserRequestDto();
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }

    protected ItemRequestDto makeItemDto(String name, String description, Boolean available, Long requestId) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(available);
        dto.setRequestId(requestId);
        return dto;
    }

    protected BookingRequestDto makeBookingDto(Long itemId, LocalDateTime start, LocalDateTime end) {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(itemId);
        dto.setStart(start);
        dto.setEnd(end);
        return dto;
    }

    protected ItemRequestRequestDto makeRequestDto(String description) {
        ItemRequestRequestDto dto = new ItemRequestRequestDto();
        dto.setDescription(description);
        return dto;
    }

    protected User persistUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        em.persist(user);
        return user;
    }

    protected Item persistItem(User owner, String name, String description, boolean available) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        em.persist(item);
        return item;
    }

    protected Item persistItem(User owner, String name, String description, boolean available,
                               ItemRequest request) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        item.setRequest(request);
        em.persist(item);
        return item;
    }

    protected Booking persistBooking(Item item, User booker, LocalDateTime start, LocalDateTime end,
                                     BookingStatus status) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(status);
        em.persist(booking);
        return booking;
    }

    protected Comment persistComment(Item item, User author, String text, LocalDateTime created) {
        Comment comment = new Comment();
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setText(text);
        comment.setCreated(created);
        em.persist(comment);
        return comment;
    }

    protected ItemRequest persistRequest(User requestor, String description, LocalDateTime created) {
        ItemRequest request = new ItemRequest();
        request.setRequestor(requestor);
        request.setDescription(description);
        request.setCreated(created);
        em.persist(request);
        return request;
    }

    protected CommentRequestDto makeCommentDto(String text) {
        CommentRequestDto dto = new CommentRequestDto();
        dto.setText(text);
        return dto;
    }
}
