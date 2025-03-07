package ru.yandex.practicum.filmorate.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;
    private InMemoryUserStorage userStorage;
    private UserService userService;
    private Map<Integer, User> users;

    @BeforeEach
    public void setUp() {
        users = new HashMap<>();
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        userController = new UserController(userStorage, userService);
    }

    @Test
    void findAll() {
        User user = new User();
        user.setLogin("User");
        user.setName("Name");
        user.setEmail("User@email.com");
        user.setBirthday(LocalDate.of(2000, 12, 11));
        User user2 = new User();
        user2.setLogin("User2");
        user2.setName("Name2");
        user2.setEmail("User2@email.com");
        user2.setBirthday(LocalDate.of(2001, 11, 14));
        User addedUser = userController.create(user);
        User addedUser2 = userController.create(user2);
        Collection<User> users = userController.findAll();
        Collection<User> usersExp = List.of(user, user2);
        Assertions.assertThat(users).containsExactlyElementsOf(usersExp);
    }

    @Test
    void createUser() {
        User user = new User();
        user.setLogin("User");
        user.setName("Name");
        user.setEmail("User@email.com");
        user.setBirthday(LocalDate.of(2000, 12, 11));
        User addedUser = userController.create(user);
        assertNotNull(addedUser);
        assertEquals("User", addedUser.getLogin());
        assertEquals("Name", addedUser.getName());
        assertEquals("User@email.com", addedUser.getEmail());
        assertEquals(LocalDate.of(2000, 12, 11), addedUser.getBirthday());
    }

    @Test
    void updateUser() {
        User user = new User();
        user.setLogin("User");
        user.setName("Name");
        user.setEmail("User@email.com");
        user.setBirthday(LocalDate.of(2000, 12, 11));
        User addedUser = userController.create(user);
        assertNotNull(addedUser);
        User newUser = new User();
        newUser.setId(1);
        newUser.setLogin("UserEDITED");
        newUser.setName("NameEDITED");
        newUser.setEmail("UserEDITED@email.com");
        newUser.setBirthday(LocalDate.of(2005, 6, 25));
        User updatedUser = userController.update(newUser);
        assertNotNull(updatedUser);
        assertEquals("UserEDITED", updatedUser.getLogin());
        assertEquals("NameEDITED", updatedUser.getName());
        assertEquals("UserEDITED@email.com", updatedUser.getEmail());
        assertEquals(LocalDate.of(2005, 6, 25), updatedUser.getBirthday());
    }

    @Test
    void createUserNoName() {
        User user = new User();
        user.setLogin("Login");
        user.setEmail("User@email.com");
        user.setBirthday(LocalDate.of(2000, 12, 11));
        User addedUser = userController.create(user);
        assertEquals("Login", addedUser.getName());
    }

}