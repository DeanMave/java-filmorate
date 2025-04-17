package ru.yandex.practicum.filmorate.storage;

import lombok.Data;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.interfaces.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.List;
import java.util.Optional;

@Data
@Repository
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;
    private static String FIND_ALL_GENRES = "SELECT * FROM genre";
    private static String FIND_GENRE_BY_ID = "SELECT * FROM genre WHERE genre_id = ?";

    @Override
    public List<Genre> getAllGenres() {
        return jdbcTemplate.query(FIND_ALL_GENRES, genreRowMapper);
    }

    @Override
    public Optional<Genre> getGenreById(Integer id) {
        try {
            Genre result = jdbcTemplate.queryForObject(FIND_GENRE_BY_ID, genreRowMapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}
