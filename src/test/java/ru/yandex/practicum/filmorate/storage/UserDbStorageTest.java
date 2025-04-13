package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(UserDbStorageTest.UserDbStorageTestConfig.class)
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userDbStorage;

    @TestConfiguration
    static class UserDbStorageTestConfig {

        @Bean
        public UserRowMapper userRowMapper() {
            return new UserRowMapper();
        }

        @Bean
        public UserDbStorage userDbStorage(JdbcTemplate jdbcTemplate, UserRowMapper userRowMapper) {
            return new UserDbStorage(jdbcTemplate, userRowMapper);
        }
    }

    @Test
    void testCreateAndFindUserById() {
        User user = new User();
        user.setName("Test User");
        user.setLogin("testlogin");
        user.setEmail("test@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userDbStorage.create(user);

        Optional<User> optionalUser = userDbStorage.findById(created.getId());

        assertThat(optionalUser)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u).hasFieldOrPropertyWithValue("id", created.getId());
                    assertThat(u).hasFieldOrPropertyWithValue("name", "Test User");
                    assertThat(u).hasFieldOrPropertyWithValue("login", "testlogin");
                    assertThat(u).hasFieldOrPropertyWithValue("email", "test@example.com");
                    assertThat(u).hasFieldOrPropertyWithValue("birthday", LocalDate.of(1990, 1, 1));
                });
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setName("Test User");
        user.setLogin("testlogin");
        user.setEmail("test@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User created = userDbStorage.create(user);

        created.setName("Updated");
        created.setEmail("new@mail.com");

        User updated = userDbStorage.update(created);
        Optional<User> optional = userDbStorage.findById(updated.getId());

        assertThat(optional)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getName()).isEqualTo("Updated");
                    assertThat(u.getEmail()).isEqualTo("new@mail.com");
                });
    }

    @Test
    void testAddAndFindFriends() {
        User user1 = new User();
        user1.setName("User1");
        user1.setLogin("login1");
        user1.setEmail("u1@mail.com");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        user1 = userDbStorage.create(user1);
        User user2 = new User();
        user2.setName("User2");
        user2.setLogin("login2");
        user2.setEmail("u2@mail.com");
        user2.setBirthday(LocalDate.of(1990, 2, 2));
        user2 = userDbStorage.create(user2);

        userDbStorage.addFriend(user1.getId(), user2.getId());

        List<User> friends = userDbStorage.findAllFriends(user1.getId());

        assertThat(friends)
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("id", user2.getId());
    }

    @Test
    void testFindCommonFriends() {
        User user1 = new User();
        user1.setName("User1");
        user1.setLogin("login1");
        user1.setEmail("u1@mail.com");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        user1 = userDbStorage.create(user1);
        User user2 = new User();
        user2.setName("User2");
        user2.setLogin("login2");
        user2.setEmail("u2@mail.com");
        user2.setBirthday(LocalDate.of(1990, 2, 2));
        user2 = userDbStorage.create(user2);
        User common = new User();
        common.setName("Common");
        common.setLogin("common");
        common.setEmail("c@mail.com");
        common.setBirthday(LocalDate.of(1992, 3, 3));
        common = userDbStorage.create(common);
        userDbStorage.addFriend(user1.getId(), common.getId());
        userDbStorage.addFriend(user2.getId(), common.getId());

        List<User> commonFriends = userDbStorage.findCommonFriends(user1.getId(), user2.getId());

        assertThat(commonFriends)
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("id", common.getId());
    }

    @Test
    void deleteUserById() {
        User user1 = new User();
        user1.setName("User1");
        user1.setLogin("login1");
        user1.setEmail("u1@mail.com");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        user1 = userDbStorage.create(user1);

        User user2 = new User();
        user2.setName("User2");
        user2.setLogin("login2");
        user2.setEmail("u2@mail.com");
        user2.setBirthday(LocalDate.of(1991, 2, 2));
        user2 = userDbStorage.create(user2);

        User user3 = new User();
        user3.setName("User3");
        user3.setLogin("login3");
        user3.setEmail("u3@mail.com");
        user3.setBirthday(LocalDate.of(1992, 3, 3));
        user3 = userDbStorage.create(user3);

        List<User> users = userDbStorage.getAllUsers();

        assertThat(users)
                .hasSize(6)
                .extracting(User::getId)
                .contains(user1.getId())
                .contains(user2.getId())
                .contains(user3.getId());

        userDbStorage.deleteUserById(user3.getId());
        users = userDbStorage.getAllUsers();

        assertThat(users)
                .hasSize(5)
                .extracting(User::getId)
                .contains(user1.getId())
                .contains(user2.getId())
                .doesNotContain(user3.getId());
    }
}
