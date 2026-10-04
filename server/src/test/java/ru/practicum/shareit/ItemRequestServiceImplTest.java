package ru.practicum.shareit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dto.ItemShortForRequestDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ItemRequestServiceImplTest extends IntegrationTest {
    private static final Long NOT_EXIST_ID = Long.MAX_VALUE;

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Test
    @DisplayName("IntegrationTest-1: addItemRequest сохраняет запрос с автором и датой создания")
    void addItemRequestPersistsRequest() {
        User requestor = persistUser("Monica", "monica@beluchi.com");
        flushAndClear();
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        ItemRequestResponseDto created = itemRequestService.addItemRequest(requestor.getId(),
                makeRequestDto("Нужна дрель"));
        flushAndClear();

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Нужна дрель");
        assertThat(created.getItems()).isEmpty();
        ItemRequest fromDb = itemRequestRepository.findById(created.getId()).orElseThrow();
        assertThat(fromDb.getDescription()).isEqualTo("Нужна дрель");
        assertThat(fromDb.getRequestor().getId()).isEqualTo(requestor.getId());
        assertThat(fromDb.getCreated()).isAfterOrEqualTo(before).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("IntegrationTest-2: то же описание от другого пользователя допустимо")
    void addItemRequestSameDescriptionDifferentUserOk() {
        User first = persistUser("Monica", "monica@beluchi.com");
        User second = persistUser("Dominica", "dominica@beluchi.com");
        persistRequest(first, "Нужна дрель", LocalDateTime.now().minusDays(1));
        flushAndClear();

        ItemRequestResponseDto created = itemRequestService.addItemRequest(second.getId(),
                makeRequestDto("Нужна дрель"));

        assertThat(created.getId()).isNotNull();
    }

    @Test
    @DisplayName("IntegrationTest-3: addItemRequest несуществующего пользователя -> NotFoundException")
    void addItemRequestUserNotFound() {
        assertThatThrownBy(() -> itemRequestService.addItemRequest(NOT_EXIST_ID, makeRequestDto("Нужна дрель")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("IntegrationTest-4: getUserRequests возвращает свои запросы, новые первыми, с предложенными вещами")
    void getUserRequestsOwnRequestsNewestFirstWithItems() {
        LocalDateTime now = LocalDateTime.now();
        User requestor = persistUser("Monica", "monica@beluchi.com");
        User owner = persistUser("Dominica", "dominica@beluchi.com");
        User other = persistUser("Veronica", "veronica@beluchi.com");
        ItemRequest older = persistRequest(requestor, "Старый запрос", now.minusDays(3));
        ItemRequest newer = persistRequest(requestor, "Новый запрос", now.minusDays(1));
        persistRequest(other, "Чужой запрос", now.minusDays(2));
        Item drill = persistItem(owner, "Дрель", "Мощная дрель", true, newer);
        flushAndClear();

        List<ItemRequestResponseDto> result = itemRequestService.getUserRequests(requestor.getId());

        assertThat(result)
                .extracting(ItemRequestResponseDto::getId)
                .containsExactly(newer.getId(), older.getId());
        assertThat(result.get(0).getItems()).hasSize(1);
        ItemShortForRequestDto offered = result.get(0).getItems().get(0);
        assertThat(offered.getId()).isEqualTo(drill.getId());
        assertThat(offered.getName()).isEqualTo("Дрель");
        assertThat(offered.getOwnerId()).isEqualTo(owner.getId());
        assertThat(result.get(1).getItems()).isEmpty();
    }

    @Test
    @DisplayName("IntegrationTest-5: getUserRequests у пользователя без запросов -> пустой список, неизвестный -> 404")
    void getUserRequestsEmptyAndNotFound() {
        User user = persistUser("Monica", "monica@beluchi.com");
        flushAndClear();

        assertThat(itemRequestService.getUserRequests(user.getId())).isEmpty();
        assertThatThrownBy(() -> itemRequestService.getUserRequests(NOT_EXIST_ID))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("IntegrationTest-6: getAllRequests возвращает запросы других пользователей, новые первыми")
    void getAllRequestsExcludesOwnAndSortsNewestFirst() {
        LocalDateTime now = LocalDateTime.now();
        User me = persistUser("Monica", "monica@beluchi.com");
        User first = persistUser("Dominica", "dominica@beluchi.com");
        User second = persistUser("Veronica", "veronica@beluchi.com");
        persistRequest(me, "Мой запрос", now.minusDays(1));
        ItemRequest older = persistRequest(first, "Запрос 1", now.minusDays(5));
        ItemRequest newer = persistRequest(second, "Запрос 2", now.minusDays(2));
        flushAndClear();

        List<ItemRequestResponseDto> result = itemRequestService.getAllRequests(me.getId(), 0, 10);

        assertThat(result)
                .extracting(ItemRequestResponseDto::getId)
                .containsExactly(newer.getId(), older.getId());
        assertThat(result)
                .extracting(ItemRequestResponseDto::getDescription)
                .doesNotContain("Мой запрос");
    }

    @Test
    @DisplayName("IntegrationTest-7: getAllRequests несуществующего пользователя -> NotFoundException")
    void getAllRequestsUserNotFound() {
        assertThatThrownBy(() -> itemRequestService.getAllRequests(NOT_EXIST_ID, 0, 10))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("IntegrationTest-8: getByRequestId возвращает запрос с вещами")
    void getByRequestIdReturnsRequestWithItems() {
        User requestor = persistUser("Monica", "monica@beluchi.com");
        User owner = persistUser("Dominica", "dominica@beluchi.com");
        LocalDateTime created = LocalDateTime.now().minusDays(1).withNano(0);
        ItemRequest request = persistRequest(requestor, "Нужна дрель", created);
        persistItem(owner, "Дрель", "Мощная дрель", true, request);
        persistItem(owner, "Отвертка", "Не по запросу", true);
        flushAndClear();

        ItemRequestResponseDto result = itemRequestService.getByRequestId(request.getId());

        assertThat(result.getId()).isEqualTo(request.getId());
        assertThat(result.getDescription()).isEqualTo("Нужна дрель");
        assertThat(result.getCreated()).isEqualTo(created);
        assertThat(result.getItems())
                .extracting(ItemShortForRequestDto::getName)
                .containsExactly("Дрель");
    }

    @Test
    @DisplayName("IntegrationTest-9: getByRequestId несуществующего запроса -> NotFoundException")
    void getByRequestIdNotFound() {
        assertThatThrownBy(() -> itemRequestService.getByRequestId(NOT_EXIST_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Запрос не найден");
    }
}
