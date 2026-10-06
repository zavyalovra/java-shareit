package ru.practicum.shareit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserServiceImplTest extends IntegrationTest {
    private static final Long NOT_EXIST_ID = Long.MAX_VALUE;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("IntegrationTest-1: createUser сохраняет пользователя в БД и возвращает id")
    void createUserPersistsUser() {
        UserResponseDto created = userService.createUser(makeUserDto("Monica", "monica@beluchi.com"));
        flushAndClear();

        assertThat(created.getId()).isNotNull();
        User fromDb = userRepository.findById(created.getId()).orElseThrow();
        assertThat(fromDb.getName()).isEqualTo("Monica");
        assertThat(fromDb.getEmail()).isEqualTo("monica@beluchi.com");
    }

    @Test
    @DisplayName("IntegrationTest-2: createUser с занятым email -> ConflictException, в БД одна запись")
    void createUser_duplicateEmail_throwsConflict() {
        userService.createUser(makeUserDto("Monica", "monica@beluchi.com"));
        flushAndClear();

        assertThatThrownBy(() -> userService.createUser(makeUserDto("Dominica", "monica@beluchi.com")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("monica@beluchi.com");

        assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("IntegrationTest-3: getAllUsers возвращает всех пользователей из БД")
    void getAllUsersReturnsAll() {
        persistUser("Monica", "monica@beluchi.com");
        persistUser("Dominica", "dominica@beluchi.com");
        flushAndClear();

        List<UserResponseDto> users = userService.getAllUsers();
        assertThat(users)
                .extracting(UserResponseDto::getEmail)
                .containsExactlyInAnyOrder("monica@beluchi.com", "dominica@beluchi.com");
    }

    @Test
    @DisplayName("IntegrationTest-4: getAllUsers при пустой БД возвращает пустой список")
    void getAllUsersEmpty() {
        assertThat(userService.getAllUsers()).isEmpty();
    }

    @Test
    @DisplayName("IntegrationTest-5: getUserById возвращает пользователя")
    void getUserByIdReturnsUser() {
        User user = persistUser("Monica", "monica@beluchi.com");
        flushAndClear();

        UserResponseDto result = userService.getUserById(user.getId());

        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getName()).isEqualTo("Monica");
        assertThat(result.getEmail()).isEqualTo("monica@beluchi.com");
    }

    @Test
    @DisplayName("IntegrationTest-6: getUserById несуществующего -> NotFoundException")
    void getUserById_notFound() {
        assertThatThrownBy(() -> userService.getUserById(NOT_EXIST_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(String.valueOf(NOT_EXIST_ID));
    }

    @Test
    @DisplayName("IntegrationTest-7: updateUser меняет только переданные поля")
    void updateUserPartialUpdate() {
        User user = persistUser("Monica", "monica@beluchi.com");
        flushAndClear();

        UserResponseDto updated = userService.updateUser(user.getId(), makeUserDto("Dominica", null));
        flushAndClear();

        assertThat(updated.getName()).isEqualTo("Dominica");
        assertThat(updated.getEmail()).isEqualTo("monica@beluchi.com");
        User fromDb = userRepository.findById(user.getId()).orElseThrow();
        assertThat(fromDb.getName()).isEqualTo("Dominica");
        assertThat(fromDb.getEmail()).isEqualTo("monica@beluchi.com");
    }

    @Test
    @DisplayName("IntegrationTest-8: updateUser с собственным email не вызывает конфликт")
    void updateUser_ownEmail_ok() {
        User user = persistUser("Monica", "monica@beluchi.com");
        flushAndClear();

        UserResponseDto updated = userService.updateUser(user.getId(), makeUserDto("Dominica", "monica@beluchi.com"));

        assertThat(updated.getEmail()).isEqualTo("monica@beluchi.com");
        assertThat(updated.getName()).isEqualTo("Dominica");
    }

    @Test
    @DisplayName("IntegrationTest-9: updateUser на email другого пользователя -> ConflictException")
    void updateUser_emailTaken_throwsConflict() {
        User firstUser = persistUser("Monica", "monica@beluchi.com");
        User secondUser = persistUser("Dominica", "dominica@beluchi.com");
        flushAndClear();

        assertThatThrownBy(() -> userService.updateUser(secondUser.getId(), makeUserDto(null, firstUser.getEmail())))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("IntegrationTest-10: updateUser несуществующего -> NotFoundException")
    void updateUserNotFound() {
        assertThatThrownBy(() -> userService.updateUser(NOT_EXIST_ID, makeUserDto("X", "x@x.ru")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("IntegrationTest-11: deleteUser удаляет пользователя из БД")
    void deleteUserRemovesUser() {
        User user = persistUser("Monica", "monica@beluchi.com");
        flushAndClear();

        userService.deleteUser(user.getId());
        flushAndClear();

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    @DisplayName("IntegrationTest-12: deleteUser каскадно удаляет вещи и бронирования пользователя")
    void deleteUserCascadesToItemsAndBookings() {
        User owner = persistUser("Owner", "owner@mail.ru");
        User booker = persistUser("Booker", "booker@mail.ru");
        Item item = persistItem(owner, "Дрель", "Мощная", true);
        persistBooking(item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING);
        flushAndClear();

        userService.deleteUser(owner.getId());
        flushAndClear();

        Long items = em.createQuery("select count(i) from Item i", Long.class).getSingleResult();
        Long bookings = em.createQuery("select count(b) from Booking b", Long.class).getSingleResult();
        assertThat(items).isZero();
        assertThat(bookings).isZero();
        assertThat(userRepository.findById(booker.getId())).isPresent();
    }
}
