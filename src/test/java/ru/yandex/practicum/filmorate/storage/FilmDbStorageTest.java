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
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(FilmDbStorageTest.FilmDbStorageTestConfig.class)
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmDbStorage;

    @Autowired
    private UserDbStorage userDbStorage;

    @TestConfiguration
    static class FilmDbStorageTestConfig {

        @Bean
        public FilmRowMapper filmRowMapper() {
            return new FilmRowMapper();
        }

        @Bean
        public GenreRowMapper genreRowMapper() {
            return new GenreRowMapper();
        }

        @Bean
        public DirectorRowMapper directorRowMapper() {
            return new DirectorRowMapper();
        }

        @Bean
        public FilmDbStorage filmDbStorage(JdbcTemplate jdbcTemplate,
                                           FilmRowMapper filmRowMapper,
                                           GenreRowMapper genreRowMapper,
                                           DirectorRowMapper directorRowMapper) {
            return new FilmDbStorage(jdbcTemplate, filmRowMapper, genreRowMapper, directorRowMapper);
        }

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
    void testCreateAndFindFilmById() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(1990, 1, 1));
        film.setDuration(60);
        Rating rating = new Rating();
        rating.setId(1);
        film.setRating(rating);

        Film created = filmDbStorage.addNewFilm(film);

        Optional<Film> optionalFilm = filmDbStorage.findById(created.getId());

        assertThat(optionalFilm)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u).hasFieldOrPropertyWithValue("id", created.getId());
                    assertThat(u).hasFieldOrPropertyWithValue("name", "Test");
                    assertThat(u).hasFieldOrPropertyWithValue("description", "Test Description");
                    assertThat(u).hasFieldOrPropertyWithValue("duration", 60);
                    assertThat(u).hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1990, 1, 1));
                    assertThat(u.getRating().getId()).isEqualTo(1);
                });
    }

    @Test
    void testUpdateFilm() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(1990, 1, 1));
        film.setDuration(60);
        Rating rating = new Rating();
        rating.setId(1);
        film.setRating(rating);

        Film created = filmDbStorage.addNewFilm(film);

        created.setName("Updated");
        created.setDescription("Updated Description");

        Film updated = filmDbStorage.updateFilm(created);
        Optional<Film> optional = filmDbStorage.findById(updated.getId());

        assertThat(optional)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getName()).isEqualTo("Updated");
                    assertThat(u.getDescription()).isEqualTo("Updated Description");
                });
    }

    @Test
    void testGetAllFilms() {
        Film film = new Film();
        film.setName("Test 1");
        film.setDescription("Test Description 1");
        film.setReleaseDate(LocalDate.of(1990, 1, 1));
        film.setDuration(60);
        Rating rating = new Rating();
        rating.setId(1);
        film.setRating(rating);
        film = filmDbStorage.addNewFilm(film);
        Film film2 = new Film();
        film2.setName("Test 2");
        film2.setDescription("Test Description 2");
        film2.setReleaseDate(LocalDate.of(1990, 2, 2));
        film2.setDuration(62);
        Rating rating2 = new Rating();
        rating2.setId(2);
        film2.setRating(rating2);
        film2 = filmDbStorage.addNewFilm(film2);

        List<Film> films = filmDbStorage.getAllFilms();

        assertThat(films)
                .hasSize(2)
                .first()
                .hasFieldOrPropertyWithValue("id", film.getId());
    }

    @Test
    void testAddLikeAndGetLikes() {
        Film film = new Film();
        film.setName("Test 1");
        film.setDescription("Test Description 1");
        film.setReleaseDate(LocalDate.of(1990, 1, 1));
        film.setDuration(60);
        Rating rating = new Rating();
        rating.setId(1);
        film.setRating(rating);
        film = filmDbStorage.addNewFilm(film);
        User user1 = new User();
        user1.setName("User1");
        user1.setLogin("login1");
        user1.setEmail("u1@mail.com");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        user1 = userDbStorage.create(user1);
        filmDbStorage.addLike(film.getId(), user1.getId());
        Set<Integer> likes = filmDbStorage.getLikesByFilmId(film.getId());

        assertThat(likes)
                .hasSize(1)
                .contains(user1.getId());
    }

    @Test
    void testFindMostPopularFilms() {
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

        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(100);
        Rating rating1 = new Rating();
        rating1.setId(1);
        film1.setRating(rating1);
        film1 = filmDbStorage.addNewFilm(film1);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2001, 2, 2));
        film2.setDuration(120);
        Rating rating2 = new Rating();
        rating2.setId(1);
        film2.setRating(rating2);
        film2 = filmDbStorage.addNewFilm(film2);

        filmDbStorage.addLike(film1.getId(), user1.getId());
        filmDbStorage.addLike(film1.getId(), user2.getId());
        filmDbStorage.addLike(film2.getId(), user1.getId());

        List<Film> popular = filmDbStorage.findMostPopularFilms();

        assertThat(popular)
                .hasSizeGreaterThanOrEqualTo(2)
                .extracting(Film::getId)
                .containsExactly(film1.getId(), film2.getId());
    }

    @Test
    void getCommonFilmsWithFriend() {
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

        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(100);
        Rating rating1 = new Rating();
        rating1.setId(1);
        film1.setRating(rating1);
        film1 = filmDbStorage.addNewFilm(film1);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2001, 2, 2));
        film2.setDuration(120);
        Rating rating2 = new Rating();
        rating2.setId(1);
        film2.setRating(rating2);
        film2 = filmDbStorage.addNewFilm(film2);

        Film film3 = new Film();
        film3.setName("Film 3");
        film3.setDescription("Description 3");
        film3.setReleaseDate(LocalDate.of(2002, 3, 3));
        film3.setDuration(100);
        Rating rating3 = new Rating();
        rating3.setId(1);
        film3.setRating(rating3);
        film3 = filmDbStorage.addNewFilm(film3);

        filmDbStorage.addLike(film1.getId(), user1.getId());
        filmDbStorage.addLike(film1.getId(), user2.getId());
        filmDbStorage.addLike(film2.getId(), user1.getId());
        filmDbStorage.addLike(film3.getId(), user2.getId());

        List<Film> popular = filmDbStorage.getCommonFilmsWithFriend(user1.getId(), user2.getId());

        assertThat(popular)
                .hasSizeGreaterThanOrEqualTo(1)
                .extracting(Film::getId)
                .containsExactly(film1.getId())
                .doesNotContain(film2.getId())
                .doesNotContain(film3.getId());
    }

    @Test
    void deleteFilmById() {
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(100);
        Rating rating1 = new Rating();
        rating1.setId(1);
        film1.setRating(rating1);
        film1 = filmDbStorage.addNewFilm(film1);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2001, 2, 2));
        film2.setDuration(120);
        Rating rating2 = new Rating();
        rating2.setId(1);
        film2.setRating(rating2);
        film2 = filmDbStorage.addNewFilm(film2);

        Film film3 = new Film();
        film3.setName("Film 3");
        film3.setDescription("Description 3");
        film3.setReleaseDate(LocalDate.of(2002, 3, 3));
        film3.setDuration(100);
        Rating rating3 = new Rating();
        rating3.setId(1);
        film3.setRating(rating3);
        film3 = filmDbStorage.addNewFilm(film3);

        List<Film> films = filmDbStorage.getAllFilms();

        assertThat(films)
                .hasSize(3)
                .extracting(Film::getId)
                .contains(film1.getId())
                .contains(film2.getId())
                .contains(film3.getId());

        filmDbStorage.deleteFilmById(film2.getId());
        films = filmDbStorage.getAllFilms();

        assertThat(films)
                .hasSize(2)
                .extracting(Film::getId)
                .contains(film1.getId())
                .doesNotContain(film2.getId())
                .contains(film3.getId());
    }

}
