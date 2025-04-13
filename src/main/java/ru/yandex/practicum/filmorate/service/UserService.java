package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import ru.yandex.practicum.filmorate.storage.mappers.EventDbStorage;

import java.util.*;

@Slf4j
@Service
public class UserService {
    private final UserDbStorage userStorage;
    private final EventDbStorage eventDbStorage;

    public UserService(UserDbStorage userStorage, EventDbStorage eventDbStorage) {
        this.userStorage = userStorage;
        this.eventDbStorage = eventDbStorage;
    }

    public void addFriend(Integer userId, Integer friendId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден."));
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + friendId + " не найден."));

        userStorage.addFriend(userId, friendId);
        eventDbStorage.addEvent(EventType.FRIEND, Operation.ADD, userId, friendId);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден."));
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + friendId + " не найден."));

        userStorage.removeFriend(userId, friendId);
        eventDbStorage.addEvent(EventType.FRIEND, Operation.REMOVE, userId, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public List<User> findAllFriends(Integer userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден."));
        return userStorage.findAllFriends(userId);
    }

    public List<User> findCommonFriends(Integer userId, Integer otherId) {
        return userStorage.findCommonFriends(userId, otherId);
    }

    public User findUser(Integer userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден."));
    }

    public List<Event> getEventFeed(Integer userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден."));
        return eventDbStorage.getEventFeed(user.getId());
    }

    public void deleteUserById(Integer userId) {
        findUser(userId);
        userStorage.deleteUserById(userId);
        log.info("Пользователь {} удалён", userId);
    }
}