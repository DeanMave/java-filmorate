package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({UserDbStorage.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userDbStorage;

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

}
