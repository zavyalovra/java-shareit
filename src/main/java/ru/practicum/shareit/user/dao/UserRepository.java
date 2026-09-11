package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> findAll();

    Optional<User> findById(Long userId);

    Optional<User> findByEmail(String email);

    User create(User user);

    User save(User user);

    void delete(Long userId);
}
