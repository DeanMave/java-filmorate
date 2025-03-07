package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final InMemoryUserStorage userStorage;

    public UserService(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        if (user == null || friend == null) {
            throw new NotFoundException("Таких пользователей не существует");
        }
        user.getFriends().add(friend.getId());
        friend.getFriends().add(user.getId());
        log.info("Пользователь добавил друга: ID = {}, Логин = {}, Имя = {}, Почта = {}, День рождения = {}",
                friend.getId(), friend.getLogin(), friend.getName(), friend.getEmail(), friend.getBirthday());
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        if (user != null && friend != null) {
            log.info("Пользователь удалил друга: ID = {}, Логин = {}, Имя = {}, Почта = {}, День рождения = {}",
                    friend.getId(), friend.getLogin(), friend.getName(), friend.getEmail(), friend.getBirthday());
            user.getFriends().remove(friend.getId());
            friend.getFriends().remove(user.getId());
        } else {
            throw new NotFoundException("Такого пользователя не существует");
        }
    }

    public List<User> findAllFriends(Integer userId) {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Такого пользователя не существует");
        }
        log.info("Список друзей пользователя: {}", user.getFriends().stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList()));
        return user.getFriends()
                .stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> findCommonFriends(Integer userId, Integer otherId) {
        User user = userStorage.getUserById(userId);
        User otherUser = userStorage.getUserById(otherId);
        if (user == null && otherUser == null) {
            throw new NotFoundException("Общих друзей не нашлось");
        }
        Set<Integer> userFriends = user.getFriends();
        Set<Integer> otherUserFriends = otherUser.getFriends();
        log.info("Список общих друзей пользователей: {}", userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(userStorage::getUserById)
                .collect(Collectors.toList()));
        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public User findUser(Integer userId) {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Такого пользователя не существует");
        }
        return user;
    }

}
