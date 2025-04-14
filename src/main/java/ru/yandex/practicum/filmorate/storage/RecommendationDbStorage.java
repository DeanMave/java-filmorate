package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.interfaces.RecommendationStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.util.List;

@Repository
public class RecommendationDbStorage implements RecommendationStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    public RecommendationDbStorage(JdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmRowMapper = filmRowMapper;
    }

    @Override
    public List<Film> getRecommendedFilms(Integer userId) {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id, " +
                "r.rating_name, " +
                "STRING_AGG(DISTINCT CAST(g.genre_id AS VARCHAR), ',') AS genre_ids, " +
                "STRING_AGG(DISTINCT g.name, ',') AS genre_names " +
                "FROM film f " +
                "LEFT JOIN rating r ON f.rating_id = r.rating_id " +
                "LEFT JOIN film_genre fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genre g ON fg.genre_id = g.genre_id " +
                "WHERE f.film_id IN ( " +
                "    SELECT l.film_id FROM likes l " +
                "    WHERE l.user_id IN ( " +
                "        SELECT l2.user_id FROM likes l1 " +
                "        JOIN likes l2 ON l1.film_id = l2.film_id " +
                "        WHERE l1.user_id = ? AND l2.user_id != ? " +
                "        GROUP BY l2.user_id ORDER BY COUNT(l2.film_id) DESC LIMIT 10 " +
                "    ) " +
                "    AND l.film_id NOT IN (SELECT film_id FROM likes WHERE user_id = ?) " +
                ") " +
                "GROUP BY f.film_id, r.rating_name";

        return jdbcTemplate.query(sql, filmRowMapper, userId, userId, userId);
    }
}
