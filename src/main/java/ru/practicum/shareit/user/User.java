package ru.practicum.shareit.user;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;

/**
 * TODO Sprint add-controllers.
 */
@Entity
@Table(name = "users", schema = "public")
@Getter
@Setter
@ToString
public class User {

    /** Уникальный идентификатор пользователя */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Имя или логин пользователя */
    @NotBlank(message = "Логин не может быть пустым")
    private String name;

    /** Уникальный адрес электронной почты */
    @NotBlank(message = "Email не может быть пустым")
    @NotNull(message = "Некорректный формат email")
    @Email(message = "Некорректный формат email")
    private String email;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return id != null && id.equals(((User) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}