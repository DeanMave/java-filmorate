package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.RecommendationService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserDbStorage userStorage;
    private final UserService userService;
    private final RecommendationService recommendationService;

    public UserController(UserDbStorage userStorage, UserService userService, RecommendationService recommendationService) {
        this.userStorage = userStorage;
        this.userService = userService;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{id}/friends")
    public List<User> findAllFriends(@PathVariable Integer id) {
        return userService.findAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> findAllFriends(@PathVariable Integer id, @PathVariable Integer otherId) {
        return userService.findCommonFriends(id, otherId);
    }

    @GetMapping
    public Collection<User> findAll() {
        return userStorage.getAllUsers();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return userStorage.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        return userStorage.update(newUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{userId}/recommendations")
    public List<Film> getRecommendations(@PathVariable Integer userId) {
        return recommendationService.getRecommendedFilms(userId);
    }
}
