package ru.practicum.shareit.user.dao.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UpdateUserRequestDto;

import java.util.*;

@Repository
@Qualifier("InMemoryUserRepository")
@Slf4j
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public List<User> findAll() {
        return users.values().stream().toList();
    }

    @Override
    public Optional<User> findById(Long userId) {
        log.info("Запрос данных пользователя с id: {}", userId);
        return Optional.ofNullable(users.get(userId));
    }

    public Optional<User> findByEmail(String email) {
        log.debug("Запрос данных пользователя с email: {}", email);
        return users.values().stream()
                .filter(user -> Objects.equals(user.getEmail(), email))
                .findFirst();
    }

    @Override
    public User create(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);

        log.info("Создание пользователя: {}", user);
        return user;
    }

    @Override
    public User update(Long userId, UpdateUserRequestDto user) {
        User updateUser = users.get(userId);
        updateUser.setName(user.getName());
        updateUser.setEmail(user.getEmail());

        log.info("Обновление данных пользователя: {}", user);
        return updateUser;
    }

    @Override
    public void delete(Long userId) {
        log.info("Удаление пользователя с id: {}", userId);
        users.remove(userId);
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
