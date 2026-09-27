package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .map(UserMapper::toResponseDto)
                .toList();
    }

    @Override
    public UserResponseDto getUserById(Long userId) {
        User user = getValidUser(userId);
        return UserMapper.toResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto createUser(UserRequestDto userDto) {
        if (userDto.getName() == null) {
            throw new ValidationException("Имя пользователя не задано");
        }
        if (userDto.getEmail() == null) {
            throw new ValidationException("Email пользователя не задан");
        }
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new ConflictException("Пользователь с email=" + userDto.getEmail() + " уже существует");
        }

        User newUser = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(newUser);

        return UserMapper.toResponseDto(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long userId, UserRequestDto userDto) {
        User user = getValidUser(userId);
        userRepository.findByEmail(userDto.getEmail())
                        .filter(u -> !u.getId().equals(userId))
                        .ifPresent(u -> {
                            throw new ConflictException(
                                    "Пользователь с email=" + userDto.getEmail() + " уже существует"
                            );
                        });

        UserMapper.applyUpdate(user, userDto);
        User savedUser = userRepository.save(user);

        return UserMapper.toResponseDto(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public User getValidUser(Long userId) {
        if (userId == null) {
            throw new ValidationException("Имя пользователя не задано");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }
}
