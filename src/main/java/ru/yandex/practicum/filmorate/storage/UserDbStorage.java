package ru.yandex.practicum.filmorate.storage;

import lombok.Data;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaces.UserStorage;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Component
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;
    private static final String INSERT_QUERY = "INSERT INTO users(name, login, email,birthday) " +
            "VALUES (?, ?, ?, ?)";
    private static final String FIND_ALL = "SELECT * FROM users";
    private static final String FIND_BY_ID = "SELECT * FROM users where user_id = ?";
    private static final String DELETE = "DELETE FROM users WHERE user_id = ?";
    private static final String UPDATE = "UPDATE users SET name = ?, login = ?, email = ?, birthday = ? WHERE user_id = ?";
    private static final String INSERT_FRIEND = "INSERT INTO friendship(user_id,friend_id,status) " +
            "VALUES (?, ?, ?)";
    private static final String DELETE_FRIEND = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_COMMON_FRIENDS = """
            SELECT u.user_id, u.name, u.email, u.login, u.birthday
            FROM users AS u
            WHERE u.user_id IN (
                SELECT f1.friend_id
                FROM friendship AS f1
                JOIN friendship AS f2 ON f1.friend_id = f2.friend_id
                WHERE f1.user_id = ? AND f2.user_id = ?
                  AND f1.status = 'CONFIRMED' AND f2.status = 'CONFIRMED'
            )
            """;
    private static final String FIND_ALL_FRIENDS = """
            SELECT u.user_id, u.name, u.email, u.login, u.birthday
            FROM users AS u
            JOIN friendship AS f ON u.user_id = f.friend_id
            WHERE f.user_id = ?
            """;

    @Override
    public List<User> getAllUsers() {
        return jdbcTemplate.query(FIND_ALL, userRowMapper);
    }

    @Override
    public Optional<User> findById(Integer id) {
        try {
            User result = jdbcTemplate.queryForObject(FIND_BY_ID, userRowMapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public User create(User user) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getEmail());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            user.setId(key.intValue());
            return user;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    @Override
    public User update(User newUser) {
        int rowsUpdated = jdbcTemplate.update(UPDATE, newUser.getName(),
                newUser.getLogin(), newUser.getEmail(), newUser.getBirthday(), newUser.getId());
        if (rowsUpdated == 0) {
            throw new NotFoundException("Пользователь с " + newUser.getId() + " не найден.");
        }
        return newUser;
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        jdbcTemplate.update(INSERT_FRIEND, userId, friendId, "CONFIRMED");
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        jdbcTemplate.update(DELETE_FRIEND, userId, friendId);
    }

    @Override
    public List<User> findCommonFriends(Integer userId, Integer friendId) {
        return jdbcTemplate.query(FIND_COMMON_FRIENDS, userRowMapper, userId, friendId);
    }

    @Override
    public List<User> findAllFriends(Integer userId) {
        return jdbcTemplate.query(FIND_ALL_FRIENDS, userRowMapper, userId);
    }

    @Override
    public void deleteUserById(Integer userId) {
        List<User> friends = findAllFriends(userId);
        for (User friend : friends) {
            removeFriend(friend.getId(), userId);
        }
        String deleteFromFriendship = "DELETE FROM friendship WHERE user_id = ? OR friend_id = ?";
        jdbcTemplate.update(deleteFromFriendship, userId, userId);
        jdbcTemplate.update(DELETE, userId);
    }
}
