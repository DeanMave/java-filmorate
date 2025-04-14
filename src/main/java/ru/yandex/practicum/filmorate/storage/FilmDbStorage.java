package ru.yandex.practicum.filmorate.storage;

import lombok.Data;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Component
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;
    private final DirectorRowMapper directorRowMapper;
    private static final String INSERT_NEW_FILM = "INSERT INTO film(name, description, duration,release_date,rating_id) " +
                                                  "VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_FILM_GENRE = "INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)";
    private static final String INSERT_FILM_DIRECTOR = "INSERT INTO film_director(film_id, director_id) VALUES (?, ?)";
    private static final String DELETE_FILM_GENRE = "DELETE FROM film_genre WHERE film_id = ?";
    private static final String DELETE_FILM_DIRECTOR = "DELETE FROM film_director WHERE film_id = ?";
    private static final String UPDATE_FILM_GENRE = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String UPDATE_FILM_DIRECTOR = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?)";
    private static final String FIND_GENRES_FILM_BY_ID = """
            SELECT g.genre_id, g.name
            FROM film_genre AS fg
            JOIN genre AS g ON fg.genre_id = g.genre_id
            WHERE fg.film_id = ?
            ORDER BY g.genre_id
            """;
    private static final String FIND_DIRECTORS_FILM_BY_ID = """
            SELECT d.director_id, d.name
            FROM film_director AS fd
            JOIN director AS d ON fd.director_id = d.director_id
            WHERE fd.film_id = ?
            """;
    private static final String FIND_GENRES_FILMS = """
            SELECT fg.film_id, g.genre_id, g.name
            FROM film_genre fg
            JOIN genre g ON fg.genre_id = g.genre_id
            """;
    private static final String ADD_LIKE_FILM = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_FILM = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String GET_LIKES_BY_FILM_ID = "SELECT user_id FROM likes WHERE film_id = ?";
    private static final String FIND_LIKES_OF_FILMS = """
            SELECT l.film_id, l.user_id
            FROM likes AS l
            JOIN film AS f ON f.film_id = l.film_id
            """;
    private static final String FIND_DIRECTOR_OF_FILMS = """
            SELECT fd.film_id, d.director_id, d.name
            FROM film_director fd
            JOIN director d ON fd.director_id = d.director_id
            """;
    private static final String FIND_ALL = """
            SELECT f.*, r.rating_name
            FROM film f
            JOIN rating r ON f.rating_id = r.rating_id
            """;
    private static final String FIND_BY_ID = """
                SELECT f.*, r.rating_name
                FROM film f
                LEFT JOIN rating r ON f.rating_id = r.rating_id
                WHERE f.film_id = ?
            """;
    private static final String DELETE = "DELETE FROM film WHERE film_id = ?";
    private static final String UPDATE = "UPDATE film SET name = ?, description = ?, duration = ?, " +
                                         "release_date = ?, rating_id = ? WHERE film_id = ?";
    private static final String FIND_MOST_POPULAR_FILMS = """
            SELECT f.film_id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.rating_id,
                       r.rating_name,
                       COUNT(l.user_id) AS likes_film
                FROM film AS f
                JOIN rating r ON f.rating_id = r.rating_id
                LEFT JOIN likes AS l ON f.film_id = l.film_id
                GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id, r.rating_name
                ORDER BY likes_film DESC
            """;

    private static final String FIND_FILMS_BY_DIRECTOR_LIKES = """
             SELECT f.film_id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.rating_id,
                       r.rating_name,
                       COUNT(l.user_id) AS likes_count
                FROM film AS f
                JOIN film_director AS fd ON f.film_id = fd.film_id
                LEFT JOIN likes AS l ON f.film_id = l.film_id
                JOIN rating r ON f.rating_id = r.rating_id
                WHERE fd.director_id = ?
                GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id, r.rating_name
                ORDER BY likes_count DESC
            """;
    private static final String FIND_FILMS_BY_DIRECTOR_YEARS = """
             SELECT f.film_id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.rating_id,
                       r.rating_name
                FROM film AS f
                JOIN film_director AS fd ON f.film_id = fd.film_id
                JOIN rating r ON f.rating_id = r.rating_id
                WHERE fd.director_id = ?
                ORDER BY f.release_date ASC
            """;
    private static final String FIND_FILM_BY_TITLE = """
                SELECT f.film_id, f.name, f.description, f.release_date, f.duration,
                       f.rating_id, r.rating_name, COUNT(l.user_id) AS likes_count
                FROM film AS f
                JOIN rating AS r ON f.rating_id = r.rating_id
                LEFT JOIN likes AS l ON f.film_id = l.film_id
                WHERE LOWER(f.name) LIKE(CONCAT('%', ?, '%'))
                GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id, r.rating_name
                ORDER BY likes_count DESC
            """;

    private static final String FIND_FILM_BY_DIRECTOR = """
                SELECT DISTINCT f.film_id, f.name, f.description, f.release_date, f.duration,
                       f.rating_id, r.rating_name, COUNT(l.user_id) AS likes_count
                FROM film AS f
                LEFT JOIN film_director fd ON f.film_id = fd.film_id
                LEFT JOIN director d ON fd.director_id = d.director_id
                JOIN rating AS r ON f.rating_id = r.rating_id
                LEFT JOIN likes AS l ON f.film_id = l.film_id
                WHERE LOWER(d.name) LIKE(CONCAT('%', ?, '%'))
                GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id, r.rating_name
                ORDER BY likes_count DESC
            """;

    private static final String FIND_FILM_BY_TITLE_DIRECTOR = """
                SELECT f.film_id, f.name, f.description, f.release_date, f.duration,
                       f.rating_id, r.rating_name, COUNT(l.user_id) AS likes_count
                FROM film AS f
                LEFT JOIN film_director fd ON f.film_id = fd.film_id
                LEFT JOIN director d ON fd.director_id = d.director_id
                JOIN rating AS r ON f.rating_id = r.rating_id
                LEFT JOIN likes AS l ON f.film_id = l.film_id
                WHERE LOWER(d.name) LIKE(CONCAT('%', ?, '%'))
                   OR LOWER(f.name) LIKE(CONCAT('%', ?, '%'))
                GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id, r.rating_name
                ORDER BY likes_count DESC
            """;

    @Override
    public List<Film> getAllFilms() {
        List<Film> films = jdbcTemplate.query(FIND_ALL, filmRowMapper);
        enrichFilm(films);
        return films;
    }

    @Override
    public List<Film> getFilmsByDirectorSortedByLikes(Integer directorId) {
        List<Film> films = jdbcTemplate.query(FIND_FILMS_BY_DIRECTOR_LIKES, filmRowMapper, directorId);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильмы с режиссёром id=" + directorId + " не найдены.");
        }
        enrichFilm(films);
        return films;
    }

    @Override
    public List<Film> getFilmsByDirectorSortedByYears(Integer directorId) {
        List<Film> films = jdbcTemplate.query(FIND_FILMS_BY_DIRECTOR_YEARS, filmRowMapper, directorId);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильмы с режиссёром id=" + directorId + " не найдены.");
        }
        enrichFilm(films);
        return films;
    }

    @Override
    public List<Film> searchByTitle(String query) {
        List<Film> films = jdbcTemplate.query(FIND_FILM_BY_TITLE, filmRowMapper, query.toLowerCase());
        enrichFilm(films);
        return films;
    }

    @Override
    public List<Film> searchByDirector(String query) {
        List<Film> films = jdbcTemplate.query(FIND_FILM_BY_DIRECTOR, filmRowMapper, query.toLowerCase());
        enrichFilm(films);
        return films;
    }

    @Override
    public List<Film> searchByTitleAndDirector(String query) {
        List<Film> films = jdbcTemplate.query(FIND_FILM_BY_TITLE_DIRECTOR, filmRowMapper, query.toLowerCase(), query.toLowerCase());
        enrichFilm(films);
        return films;
    }

    @Override
    public Film addNewFilm(Film film) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_NEW_FILM, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setInt(3, film.getDuration());
            ps.setDate(4, Date.valueOf(film.getReleaseDate()));
            if (film.getRating() == null) {
                throw new RuntimeException("Поле rating (mpa) обязательно для заполнения.");
            }
            ps.setInt(5, film.getRating().getId());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            film.setId(key.intValue());
            if (film.getGenres() != null) {
                List<Genre> genreList = new ArrayList<>(film.getGenres()); // без сортировки!
                jdbcTemplate.batchUpdate(INSERT_FILM_GENRE,
                        new BatchPreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps, int i) throws SQLException {
                                Genre genre = genreList.get(i);
                                ps.setInt(1, film.getId());
                                ps.setInt(2, genre.getId());
                            }

                            @Override
                            public int getBatchSize() {
                                return genreList.size();
                            }
                        }
                );
            }
            if (film.getDirectors() != null) {
                jdbcTemplate.batchUpdate(INSERT_FILM_DIRECTOR,
                        new BatchPreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps, int i) throws SQLException {
                                Director director = new ArrayList<>(film.getDirectors()).get(i);
                                ps.setInt(1, film.getId());
                                ps.setInt(2, director.getId());
                            }

                            @Override
                            public int getBatchSize() {
                                return film.getDirectors().size();
                            }
                        });
            }
            return film;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    @Override
    public Film updateFilm(Film newFilm) {
        int rowsUpdated = jdbcTemplate.update(UPDATE, newFilm.getName(),
                newFilm.getDescription(), newFilm.getDuration(), newFilm.getReleaseDate(), newFilm.getRating().getId(),
                newFilm.getId());
        if (rowsUpdated == 0) {
            throw new NotFoundException("Фильм с " + newFilm.getId() + " не найден.");
        }
        jdbcTemplate.update(DELETE_FILM_GENRE, newFilm.getId());
        if (newFilm.getGenres() != null) {
            List<Genre> sortedGenres = newFilm.getGenres().stream()
                    .sorted(Comparator.comparing(Genre::getId))
                    .collect(Collectors.toList());
            jdbcTemplate.batchUpdate(UPDATE_FILM_GENRE,
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Genre genre = sortedGenres.get(i);
                            ps.setInt(1, newFilm.getId());
                            ps.setInt(2, genre.getId());
                        }

                        @Override
                        public int getBatchSize() {
                            return newFilm.getGenres().size();
                        }
                    }
            );
        }
        jdbcTemplate.update(DELETE_FILM_DIRECTOR, newFilm.getId());
        if (newFilm.getDirectors() != null) {
            jdbcTemplate.batchUpdate(UPDATE_FILM_DIRECTOR,
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Director director = new ArrayList<>(newFilm.getDirectors()).get(i);
                            ps.setInt(1, newFilm.getId());
                            ps.setInt(2, director.getId());
                        }

                        @Override
                        public int getBatchSize() {
                            return newFilm.getDirectors().size();
                        }
                    });
        }
        return findById(newFilm.getId()).orElseThrow(() ->
                new NotFoundException("Не удалось найти обновлённый фильм с id " + newFilm.getId()));
    }

    @Override
    public Optional<Film> findById(Integer id) {
        try {
            Film result = jdbcTemplate.queryForObject(FIND_BY_ID, filmRowMapper, id);
            if (result != null) {
                result.setLikes(getLikesByFilmId(id));
                result.setGenres(getGenresByFilmId(id));
                result.setDirectors(getDirectorsByFilmId(id));
            }
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> findMostPopularFilms() {
        return jdbcTemplate.query(FIND_MOST_POPULAR_FILMS, filmRowMapper);
    }

    @Override
    public void addLike(Integer filmId, Integer userID) {
        jdbcTemplate.update(ADD_LIKE_FILM, filmId, userID);
    }

    @Override
    public void deleteLike(Integer filmId, Integer userID) {
        jdbcTemplate.update(DELETE_LIKE_FILM, filmId, userID);
    }

    @Override
    public void deleteFilmById(Integer filmId) {
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ?", filmId);
        jdbcTemplate.update(DELETE_FILM_GENRE, filmId);
        jdbcTemplate.update(DELETE_FILM_DIRECTOR, filmId);
        jdbcTemplate.update(DELETE, filmId);
    }

    @Override
    public Set<Integer> getLikesByFilmId(Integer filmId) {
        return new HashSet<>(jdbcTemplate.query(GET_LIKES_BY_FILM_ID,
                (rs, rowNum) -> rs.getInt("user_id"), filmId));
    }

    @Override
    public Set<Genre> getGenresByFilmId(Integer filmId) {
        List<Genre> genres = jdbcTemplate.query(
                FIND_GENRES_FILM_BY_ID,
                genreRowMapper,
                filmId
        );
        genres.sort(Comparator.comparing(Genre::getId));
        return new LinkedHashSet<>(genres);
    }

    @Override
    public Set<Director> getDirectorsByFilmId(Integer filmId) {
        return new HashSet<>(jdbcTemplate.query(FIND_DIRECTORS_FILM_BY_ID, directorRowMapper, filmId));
    }

    @Override
    public List<Film> getCommonFilmsWithFriend(Integer userId, Integer friendId) {
        String sql = """
                    SELECT f.*, r.rating_name, g.genre_id
                    FROM likes l1
                    JOIN likes l2 ON l1.film_id = l2.film_id
                    JOIN film f ON l1.film_id = f.film_id
                    LEFT JOIN rating r ON f.rating_id = r.rating_id
                    LEFT JOIN film_genre fg ON f.film_id = fg.film_id
                    LEFT JOIN genre g ON fg.genre_id = g.genre_id
                    WHERE l1.user_id = ? AND l2.user_id = ?
                """;
        List<Film> commonFilms = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = filmRowMapper.mapRow(rs, rowNum);
            Set<Genre> genres = new HashSet<>();
            do {
                Integer genreId = rs.getInt("genre_id");
                if (!rs.wasNull()) {
                    Genre genre = new Genre();
                    genre.setId(genreId);
                    genres.add(genre);
                }
            } while (rs.next() && rs.getInt("film_id") == film.getId());
            film.setGenres(genres);
            return film;
        }, userId, friendId);
        return commonFilms.stream().toList();
    }

    private void enrichFilm(List<Film> films) {
        Map<Integer, Set<Genre>> filmGenres = new HashMap<>();
        jdbcTemplate.query(FIND_GENRES_FILMS,
                (rs) -> {
                    int filmId = rs.getInt("film_id");
                    Genre genre = new Genre();
                    genre.setId(rs.getInt("genre_id"));
                    genre.setName(rs.getString("name"));
                    filmGenres.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
                });

        Map<Integer, Set<Integer>> filmLikes = new HashMap<>();
        jdbcTemplate.query(FIND_LIKES_OF_FILMS,
                (rs) -> {
                    int filmId = rs.getInt("film_id");
                    filmLikes.computeIfAbsent(filmId, k -> new HashSet<>()).add(rs.getInt("user_id"));
                });

        Map<Integer, Set<Director>> filmDirectors = new HashMap<>();
        jdbcTemplate.query(FIND_DIRECTOR_OF_FILMS,
                (rs) -> {
                    int filmId = rs.getInt("film_id");
                    Director director = new Director();
                    director.setId(rs.getInt("director_id"));
                    director.setName(rs.getString("name"));
                    filmDirectors.computeIfAbsent(filmId, k -> new HashSet<>()).add(director);
                });

        for (Film film : films) {
            int id = film.getId();
            film.setGenres(filmGenres.getOrDefault(id, new HashSet<>()));
            film.setLikes(filmLikes.getOrDefault(id, new HashSet<>()));
            film.setDirectors(filmDirectors.getOrDefault(id, new HashSet<>()));
        }
    }
}
