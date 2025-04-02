package ru.yandex.practicum.filmorate.storage;

import lombok.Data;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.interfaces.RatingStorage;
import ru.yandex.practicum.filmorate.storage.mappers.RatingRowMapper;

import java.util.List;
import java.util.Optional;

@Data
@Component
public class RatingDbStorage implements RatingStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RatingRowMapper ratingRowMapper;
    private static String FIND_ALL_RATINGS = "SELECT * FROM rating";
    private static String FIND_RATING_BY_ID = "SELECT * FROM rating WHERE rating_id = ?";

    @Override
    public List<Rating> gelAllRatings() {
        return jdbcTemplate.query(FIND_ALL_RATINGS, ratingRowMapper);
    }

    @Override
    public Optional<Rating> getRatingById(Integer id) {
        try {
            Rating result = jdbcTemplate.queryForObject(FIND_RATING_BY_ID, ratingRowMapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}
