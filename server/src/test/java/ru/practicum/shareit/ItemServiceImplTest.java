package ru.practicum.shareit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.*;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemShortResponseDto;
import ru.practicum.shareit.item.dto.UpdateItemRequestDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class ItemServiceImplTest extends IntegrationTest {
    private static final Long NOT_EXIST_ID = Long.MAX_VALUE;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("IntegrationTest-1: getByOwnerId возвращает вещи владельца с last/next бронированием и комментариями")
    void getByOwnerIdReturnsItemsWithBookingsAndComments() {
        LocalDateTime now = LocalDateTime.now();
        User owner = persistUser("Monica", "monica@beluchi.com");
        User booker = persistUser("Dominica", "dominica@beluchi.com");
        User stranger = persistUser("Veronica", "veronica@beluchi.com");
        Item drill = persistItem(owner, "Дрель", "Мощная дрель", true);
        Item saw = persistItem(owner, "Пила", "Бензопила", true);
        persistItem(stranger, "Чужая вещь", "не должна попасть в ответ", true);

        persistBooking(drill, booker, now.minusDays(10), now.minusDays(9), BookingStatus.APPROVED);
        persistBooking(drill, booker, now.minusDays(3), now.minusDays(2), BookingStatus.APPROVED);
        persistBooking(drill, booker, now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED);
        persistBooking(drill, booker, now.plusDays(5), now.plusDays(6), BookingStatus.APPROVED);
        persistBooking(drill, booker, now.plusHours(1), now.plusHours(2), BookingStatus.WAITING);
        persistComment(drill, booker, "Второй", now.minusDays(1));
        persistComment(drill, booker, "Первый", now.minusDays(2));
        flushAndClear();

        List<ItemResponseDto> result = itemService.getByOwnerId(owner.getId());

        assertThat(result).hasSize(2);
        ItemResponseDto drillDto = result.stream()
                .filter(i -> i.getId().equals(drill.getId())).findFirst().orElseThrow();
        ItemResponseDto sawDto = result.stream()
                .filter(i -> i.getId().equals(saw.getId())).findFirst().orElseThrow();

        assertThat(drillDto.getLastBooking()).isNotNull();
        assertThat(drillDto.getLastBooking().getStart()).isCloseTo(now.minusDays(3), within(1, ChronoUnit.SECONDS));
        assertThat(drillDto.getNextBooking()).isNotNull();
        assertThat(drillDto.getNextBooking().getStart()).isCloseTo(now.plusDays(1), within(1, ChronoUnit.SECONDS));
        assertThat(drillDto.getComments())
                .extracting(CommentResponseDto::getText)
                .containsExactly("Первый", "Второй");
        assertThat(drillDto.getComments())
                .extracting(CommentResponseDto::getAuthorName)
                .containsOnly("Dominica");

        assertThat(sawDto.getLastBooking()).isNull();
        assertThat(sawDto.getNextBooking()).isNull();
        assertThat(sawDto.getComments()).isEmpty();
    }

    @Test
    @DisplayName("IntegrationTest-2: getByOwnerId у пользователя без вещей возвращает пустой список")
    void getByOwnerIdNoItemsReturnsEmpty() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        flushAndClear();

        assertThat(itemService.getByOwnerId(owner.getId())).isEmpty();
    }

    @Test
    @DisplayName("IntegrationTest-3: getByOwnerId несуществующего пользователя -> NotFoundException")
    void getByOwnerIdUserNotFound() {
        assertThatThrownBy(() -> itemService.getByOwnerId(NOT_EXIST_ID))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("IntegrationTest-4: getItemById для владельца содержит lastBooking/nextBooking")
    void getItemByIdOwnerSeesBookings() {
        LocalDateTime now = LocalDateTime.now();
        User owner = persistUser("Monica", "monica@beluchi.com");
        User booker = persistUser("Dominica", "dominica@beluchi.com");
        Item item = persistItem(owner, "Дрель", "Мощная дрель", true);
        persistBooking(item, booker, now.minusDays(3), now.minusDays(2), BookingStatus.APPROVED);
        persistBooking(item, booker, now.plusDays(2), now.plusDays(3), BookingStatus.APPROVED);
        persistComment(item, booker, "Хорошая вещь", now.minusDays(1));
        flushAndClear();

        ItemResponseDto result = itemService.getItemById(item.getId(), owner.getId());

        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getNextBooking()).isNotNull();
        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().get(0).getAuthorName()).isEqualTo("Dominica");
    }

    @Test
    @DisplayName("IntegrationTest-5: getItemById для не владельца скрывает бронирования, но показывает комментарии")
    void getItemByIdNotOwnerHidesBookings() {
        LocalDateTime now = LocalDateTime.now();
        User owner = persistUser("Monica", "monica@beluchi.com");
        User booker = persistUser("Dominica", "dominica@beluchi.com");
        Item item = persistItem(owner, "Дрель", "Мощная дрель", true);
        persistBooking(item, booker, now.minusDays(3), now.minusDays(2), BookingStatus.APPROVED);
        persistBooking(item, booker, now.plusDays(2), now.plusDays(3), BookingStatus.APPROVED);
        persistComment(item, booker, "Хорошая вещь", now.minusDays(1));
        flushAndClear();

        ItemResponseDto result = itemService.getItemById(item.getId(), booker.getId());

        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
        assertThat(result.getComments()).hasSize(1);
    }

    @Test
    @DisplayName("IntegrationTest-6: getItemById несуществующей вещи -> NotFoundException")
    void getItemByIdNotFound() {
        User user = persistUser("Monica", "monica@beluchi.com");
        flushAndClear();

        assertThatThrownBy(() -> itemService.getItemById(NOT_EXIST_ID, user.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("IntegrationTest-7: createItem сохраняет вещь с владельцем в БД")
    void createItemPersistsItem() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        flushAndClear();

        ItemShortResponseDto created = itemService.createItem(owner.getId(),
                makeItemDto("Дрель", "Мощная дрель", true, null));
        flushAndClear();

        Item fromDb = itemRepository.findById(created.getId()).orElseThrow();
        assertThat(fromDb.getName()).isEqualTo("Дрель");
        assertThat(fromDb.getDescription()).isEqualTo("Мощная дрель");
        assertThat(fromDb.getAvailable()).isTrue();
        assertThat(fromDb.getOwner().getId()).isEqualTo(owner.getId());
        assertThat(fromDb.getRequest()).isNull();
    }

    @Test
    @DisplayName("IntegrationTest-8: createItem с requestId связывает вещь с запросом")
    void createItemWithRequestLinksToRequest() {
        User requestor = persistUser("Dominica", "dominica@beluchi.com");
        User owner = persistUser("Monica", "monica@beluchi.com");
        ItemRequest request = persistRequest(requestor, "Нужна дрель", LocalDateTime.now().minusDays(1));
        flushAndClear();

        ItemShortResponseDto created = itemService.createItem(owner.getId(),
                makeItemDto("Дрель", "Мощная дрель", true, request.getId()));
        flushAndClear();

        Item fromDb = itemRepository.findById(created.getId()).orElseThrow();
        assertThat(fromDb.getRequest().getId()).isEqualTo(request.getId());
    }

    @Test
    @DisplayName("IntegrationTest-9: createItem с несуществующим requestId или владельцем -> NotFoundException")
    void createItemNotFoundReferences() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        flushAndClear();

        assertThatThrownBy(() -> itemService.createItem(owner.getId(), makeItemDto("Дрель", "Мощная", true, NOT_EXIST_ID)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Запрос не найден");
        assertThatThrownBy(() -> itemService.createItem(NOT_EXIST_ID, makeItemDto("Дрель", "Мощная", true, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("IntegrationTest-10: updateItem меняет только переданные поля")
    void updateItemPartialUpdate() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        Item item = persistItem(owner, "Дрель", "Мощная дрель", true);
        flushAndClear();

        UpdateItemRequestDto update = new UpdateItemRequestDto();
        update.setAvailable(false);
        update.setName("Новая дрель");
        ItemShortResponseDto result = itemService.updateItem(item.getId(), owner.getId(), update);
        flushAndClear();

        assertThat(result.getName()).isEqualTo("Новая дрель");
        Item fromDb = itemRepository.findById(item.getId()).orElseThrow();
        assertThat(fromDb.getName()).isEqualTo("Новая дрель");
        assertThat(fromDb.getDescription()).isEqualTo("Мощная дрель");
        assertThat(fromDb.getAvailable()).isFalse();
    }

    @Test
    @DisplayName("IntegrationTest-11: updateItem не владельцем -> ForbiddenException, вещь не изменена")
    void updateItemNotOwnerThrowsForbidden() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        User stranger = persistUser("Dominica", "dominica@beluchi.com");
        Item item = persistItem(owner, "Дрель", "Мощная дрель", true);
        flushAndClear();

        UpdateItemRequestDto update = new UpdateItemRequestDto();
        update.setName("Не владелец");

        assertThatThrownBy(() -> itemService.updateItem(item.getId(), stranger.getId(), update))
                .isInstanceOf(ForbiddenException.class);
        flushAndClear();
        assertThat(itemRepository.findById(item.getId()).orElseThrow().getName()).isEqualTo("Дрель");
    }

    @Test
    @DisplayName("IntegrationTest-12: updateItem несуществующей вещи -> NotFoundException")
    void updateItemNotFound() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        flushAndClear();

        assertThatThrownBy(() -> itemService.updateItem(NOT_EXIST_ID, owner.getId(), new UpdateItemRequestDto()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("IntegrationTest-13: searchItems ищет по названию и описанию без учёта регистра, только доступные")
    void searchItemsMatchesCaseInsensitiveOnlyAvailable() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        persistItem(owner, "Дрель Bosch", "Для сверления", true);
        persistItem(owner, "Отвертка", "Можно использовать как ДРЕЛЬ-шуруповерт", true);
        persistItem(owner, "Дрель сломанная", "Недоступна", false);
        persistItem(owner, "Молоток", "Забивает гвозди", true);
        flushAndClear();

        List<ItemShortResponseDto> result = itemService.searchItems("дрель");

        assertThat(result)
                .extracting(ItemShortResponseDto::getName)
                .containsExactlyInAnyOrder("Дрель Bosch", "Отвертка");
        assertThat(result).allMatch(ItemShortResponseDto::getAvailable);
    }

    @Test
    @DisplayName("IntegrationTest-14: searchItems с пустым, пробельным или null текстом возвращает пустой список")
    void searchItemsBlankTextReturnsEmpty() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        persistItem(owner, "Дрель", "Мощная", true);
        flushAndClear();

        assertThat(itemService.searchItems("")).isEmpty();
        assertThat(itemService.searchItems("   ")).isEmpty();
        assertThat(itemService.searchItems(null)).isEmpty();
    }

    @Test
    @DisplayName("IntegrationTest-15: searchItems без совпадений возвращает пустой список")
    void searchItemsNoMatches() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        persistItem(owner, "Дрель", "Мощная", true);
        flushAndClear();

        assertThat(itemService.searchItems("рояль")).isEmpty();
    }

    @Test
    @DisplayName("IntegrationTest-16: createComment сохраняет комментарий при завершённой подтверждённой брони")
    void createCommentPersistsComment() {
        LocalDateTime now = LocalDateTime.now();
        User owner = persistUser("Monica", "monica@beluchi.com");
        User booker = persistUser("Dominica", "dominica@beluchi.com");
        Item item = persistItem(owner, "Дрель", "Мощная дрель", true);
        persistBooking(item, booker, now.minusDays(3), now.minusDays(2), BookingStatus.APPROVED);
        flushAndClear();

        CommentResponseDto result = itemService.createComment(item.getId(), booker.getId(), makeCommentDto("Отличная"));
        flushAndClear();

        assertThat(result.getId()).isNotNull();
        assertThat(result.getText()).isEqualTo("Отличная");
        assertThat(result.getAuthorName()).isEqualTo("Dominica");
        assertThat(result.getCreated()).isNotNull();
        Comment fromDb = commentRepository.findById(result.getId()).orElseThrow();
        assertThat(fromDb.getItem().getId()).isEqualTo(item.getId());
        assertThat(fromDb.getAuthor().getId()).isEqualTo(booker.getId());
    }

    @Test
    @DisplayName("IntegrationTest-17: createComment без брони -> ValidationException, комментарий не сохраняется")
    void createCommentNoBookingThrowsValidation() {
        User owner = persistUser("Monica", "monica@beluchi.com");
        User stranger = persistUser("Dominica", "dominica@beluchi.com");
        Item item = persistItem(owner, "Дрель", "Мощная дрель", true);
        flushAndClear();

        assertThatThrownBy(() -> itemService.createComment(item.getId(), stranger.getId(), makeCommentDto("Текст")))
                .isInstanceOf(ValidationException.class);

        assertThat(commentRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("IntegrationTest-18: createComment при неподтверждённой или незавершённой брони -> ValidationException")
    void createCommentBookingNotApprovedOrNotFinishedThrowsValidation() {
        LocalDateTime now = LocalDateTime.now();
        User owner = persistUser("Monica", "monica@beluchi.com");
        User waiting = persistUser("Dominica", "dominica@beluchi.com");
        User current = persistUser("Veronica", "veronica@beluchi.com");
        Item item = persistItem(owner, "Дрель", "Мощная дрель", true);
        persistBooking(item, waiting, now.minusDays(3), now.minusDays(2), BookingStatus.WAITING);
        persistBooking(item, current, now.minusDays(1), now.plusDays(1), BookingStatus.APPROVED);
        flushAndClear();

        assertThatThrownBy(() -> itemService.createComment(item.getId(), waiting.getId(), makeCommentDto("Текст")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> itemService.createComment(item.getId(), current.getId(), makeCommentDto("Текст")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("IntegrationTest-19: createComment для несуществующей вещи -> NotFoundException")
    void createCommentNotFound() {
        User user = persistUser("Monica", "monica@beluchi.com");
        flushAndClear();

        assertThatThrownBy(() -> itemService.createComment(NOT_EXIST_ID, user.getId(), makeCommentDto("Текст")))
                .isInstanceOf(NotFoundException.class);
    }
}
