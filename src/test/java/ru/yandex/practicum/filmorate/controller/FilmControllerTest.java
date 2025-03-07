package ru.yandex.practicum.filmorate.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;
    private InMemoryFilmStorage filmStorage;
    private FilmService filmService;
    private InMemoryUserStorage userStorage;
    private UserService userService;
    private Map<Integer, Film> films;

    @BeforeEach
    public void setUp() {
        films = new HashMap<>();
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        filmStorage = new InMemoryFilmStorage();
        filmService = new FilmService(userService, filmStorage);
        filmController = new FilmController(filmStorage, filmService);
    }

    @Test
    void findAll() {
        Film film = new Film();
        film.setName("Film 1");
        film.setDescription("Description of Film 1");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Film film2 = new Film();
        film2.setName("Film 1");
        film2.setDescription("Description of Film 2");
        film2.setReleaseDate(LocalDate.of(2002, 5, 7));
        film2.setDuration(144);
        Film addedFilm = filmController.addNewFilm(film);
        Film addedFilm2 = filmController.addNewFilm(film2);
        Collection<Film> films = filmController.getAllFilms();
        Collection<Film> filmsExp = List.of(film, film2);
        Assertions.assertThat(films).containsExactlyElementsOf(filmsExp);
    }

    @Test
    void addNewFilm() {
        Film film = new Film();
        film.setName("Film 1");
        film.setDescription("Description of Film 1");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Film addedFilm = filmController.addNewFilm(film);
        assertNotNull(addedFilm);
        assertEquals("Film 1", addedFilm.getName());
        assertEquals("Description of Film 1", addedFilm.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), addedFilm.getReleaseDate());
        assertEquals(120, addedFilm.getDuration());
    }

    @Test
    void updateFilm() {
        Film film = new Film();
        film.setName("Film 1");
        film.setDescription("Description of Film 1");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Film addedFilm = filmController.addNewFilm(film);
        assertNotNull(addedFilm);
        Film newFilm = new Film();
        newFilm.setId(1);
        newFilm.setName("Film 1");
        newFilm.setDescription("Description of Film 1 EDITED");
        newFilm.setReleaseDate(LocalDate.of(2003, 2, 5));
        newFilm.setDuration(134);
        Film updatedFilm = filmController.updateFilm(newFilm);
        assertNotNull(updatedFilm);
        assertEquals("Film 1", updatedFilm.getName());
        assertEquals("Description of Film 1 EDITED", updatedFilm.getDescription());
        assertEquals(LocalDate.of(2003, 2, 5), updatedFilm.getReleaseDate());
        assertEquals(134, updatedFilm.getDuration());
    }

}