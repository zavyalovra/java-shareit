package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.util.List;

public interface UserService {

    User getValidUser(Long userId);

    List<UserResponseDto> getAllUsers();

    UserResponseDto getUserById(Long userId);

    UserResponseDto createUser(UserRequestDto userDto);

    UserResponseDto updateUser(Long userId, UserRequestDto userDto);

    void deleteUser(Long userId);
}
