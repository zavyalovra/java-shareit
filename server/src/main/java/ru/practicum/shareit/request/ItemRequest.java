package ru.practicum.shareit.request;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@Entity
@Table(name = "requests", schema = "public")
@Getter
@Setter
@ToString
public class ItemRequest {

    /** Уникальный идентификатор запроса */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Текст запроса, содержащий описание требуемой вещи */
    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = 1000, message = "Максимальная длина описания 1000 символов")
    private String description;

    /** Пользователь, создавший запрос */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requestor_id")
    @ToString.Exclude
    private User requestor;

    /** Дата и время создания запроса */
    private LocalDateTime created;

    /** Вещи, связанные с запросом */
    @OneToMany
    @JoinColumn(name = "request_id")
    private List<Item> items = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemRequest)) return false;
        return id != null && id.equals(((ItemRequest) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
