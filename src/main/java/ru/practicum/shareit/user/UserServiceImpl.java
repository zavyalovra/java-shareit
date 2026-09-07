package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UpdateUserRequestDto;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<User> getAllUsers() {
        return repository.findAll();
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return repository.findById(userId);
    }

    @Override
    public User createUser(User user) {
        if (repository.findByEmail(user.getEmail()).isPresent()) {
            throw new ConflictException("Пользователь с email=" + user.getEmail() + " уже существует");
        }
        return repository.create(user);
    }

    @Override
    public User updateUser(Long userId, UpdateUserRequestDto user) {
        if (repository.findByEmail(user.getEmail()).isPresent()) {
            throw new ConflictException("Пользователь с email=" + user.getEmail() + " уже существует");
        }
        return repository.update(userId, user);
    }

    @Override
    public void deleteUser(Long userId) {
        repository.delete(userId);
    }
}
