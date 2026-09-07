package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UpdateUserRequestDto;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> getAllUsers();

    Optional<User> getUserById(Long userId);

    User createUser(User user);

    User updateUser(Long userId, UpdateUserRequestDto user);

    void deleteUser(Long userId);
}
