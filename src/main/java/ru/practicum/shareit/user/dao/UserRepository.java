package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UpdateUserRequestDto;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> findAll();

    Optional<User> findById(Long userId);

    Optional<User> findByEmail(String email);

    User create(User user);

    User update(Long userId, UpdateUserRequestDto user);

    void delete(Long userId);
}
