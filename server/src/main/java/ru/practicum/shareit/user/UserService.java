package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {

    User getValidUser(Long userId);

    List<UserResponseDto> getAllUsers();

    UserResponseDto getUserById(Long userId);

    UserResponseDto createUser(UserRequestDto userDto);

    UserResponseDto updateUser(Long userId, UserRequestDto userDto);

    void deleteUser(Long userId);
}
