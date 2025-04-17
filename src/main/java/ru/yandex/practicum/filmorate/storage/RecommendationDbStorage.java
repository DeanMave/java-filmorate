package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.interfaces.RecommendationStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class RecommendationDbStorage implements RecommendationStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;

    public RecommendationDbStorage(JdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper, GenreRowMapper genreRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmRowMapper = filmRowMapper;
        this.genreRowMapper = genreRowMapper;
    }

    @Override
    public List<Film> getRecommendedFilms(Integer userId) {
        String sql = "SELECT f.*, r.rating_name " +
                     "FROM film f " +
                     "LEFT JOIN rating r ON f.rating_id = r.rating_id " +
                     "WHERE f.film_id IN ( " +
                     "    SELECT l.film_id FROM likes l " +
                     "    WHERE l.user_id IN ( " +
                     "        SELECT l2.user_id FROM likes l1 " +
                     "        JOIN likes l2 ON l1.film_id = l2.film_id " +
                     "        WHERE l1.user_id = ? AND l2.user_id != ? " +
                     "        GROUP BY l2.user_id ORDER BY COUNT(l2.film_id) DESC LIMIT 10 " +
                     "    ) " +
                     "    AND l.film_id NOT IN (SELECT film_id FROM likes WHERE user_id = ?) " +
                     ")";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, userId, userId, userId);
        Map<Integer, Set<Genre>> filmGenres = getGenresForFilms(films);

        for (Film film : films) {
            film.setGenres(filmGenres.getOrDefault(film.getId(), new HashSet<>()));
        }

        return films;
    }

    private Map<Integer, Set<Genre>> getGenresForFilms(List<Film> films) {
        if (films.isEmpty()) return Collections.emptyMap();

        String sql = "SELECT fg.film_id, g.genre_id, g.name " +
                     "FROM film_genre fg " +
                     "JOIN genre g ON fg.genre_id = g.genre_id " +
                     "WHERE fg.film_id IN (%s)";

        String inSql = films.stream().map(f -> "?").collect(Collectors.joining(", "));
        String finalSql = String.format(sql, inSql);
        Object[] params = films.stream().map(Film::getId).toArray();

        Map<Integer, Set<Genre>> result = new HashMap<>();

        jdbcTemplate.query(finalSql, params, rs -> {
            int filmId = rs.getInt("film_id");
            Genre genre = genreRowMapper.mapRow(rs, rs.getRow());
            result.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
        });

        return result;
    }
}