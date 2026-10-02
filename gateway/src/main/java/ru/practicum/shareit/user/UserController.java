package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> findAllUsers() {
        log.info("Get request for findAllUsers");
        return userClient.findAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findUserById(@PathVariable Long id) {
        log.info("Find User by id={}", id);
        return userClient.findUserById(id);
    }

    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserCreateDto requestDto) {
        log.info("Create user {}", requestDto);
        return userClient.createUser(requestDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable Long id,
                                             @Valid @RequestBody UserUpdateDto requestDto) {
        log.info("Update user {}", requestDto);
        return userClient.updateUser(id, requestDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long id) {
        log.info("Delete User by id={}", id);
        return userClient.deleteUser(id);
    }
}
