package ru.yandex.practicum.filmorate.storage;

import lombok.Data;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.interfaces.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Data
@Component
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;
    private final DirectorRowMapper directorRowMapper;
    private static String FIND_ALL_DIRECTORS = "SELECT * FROM director";
    private static String FIND_DIRECTOR_BY_ID = "SELECT * FROM director WHERE director_id = ?";
    private static String UPDATE = "UPDATE director SET name = ? WHERE director_id = ?";
    private static String DELETE = "DELETE FROM director WHERE director_id = ?";
    private static final String INSERT_NEW_DIRECTOR = "INSERT INTO director(name) " +
                                                      "VALUES (?)";

    @Override
    public List<Director> getAllDirectors() {
        return jdbcTemplate.query(FIND_ALL_DIRECTORS, directorRowMapper);
    }

    @Override
    public Optional<Director> findById(Integer id) {
        try {
            Director result = jdbcTemplate.queryForObject(FIND_DIRECTOR_BY_ID, directorRowMapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Director addNewDirector(Director director) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_NEW_DIRECTOR, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            director.setId(key.intValue());
            return director;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    @Override
    public Director updateDirector(Director newDirector) {
        int rowUpdated = jdbcTemplate.update(UPDATE, newDirector.getName(), newDirector.getId());
        if (rowUpdated == 0) {
            throw new NotFoundException("Режиссер с " + newDirector.getId() + " не найден.");
        }
        return newDirector;
    }

    @Override
    public void deleteDirector(Integer id) {
        jdbcTemplate.update(DELETE, id);
    }
}
