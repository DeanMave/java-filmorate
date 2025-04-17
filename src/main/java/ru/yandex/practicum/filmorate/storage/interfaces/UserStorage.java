package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    List<User> getAllUsers();

    Optional<User> findById(Integer id);

    User create(User user);

    User update(User newUser);

    void addFriend(Integer userId,Integer friendId);

    void removeFriend(Integer userId,Integer friendId);

    List<User> findCommonFriends(Integer userId,Integer friendId);

    List<User> findAllFriends(Integer userId);

    void deleteUserById(Integer userId);
}
