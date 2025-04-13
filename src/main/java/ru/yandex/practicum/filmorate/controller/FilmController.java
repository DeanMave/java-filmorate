package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.interfaces.FilmStorage;


import java.util.Collection;
import java.util.List;


@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmStorage filmStorage;
    private final FilmService filmService;

    public FilmController(FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @GetMapping("/popular")
    public List<Film> mostPopularFilms(@RequestParam(defaultValue = "10") int count,
                                       @RequestParam(required = false) Integer genreId,
                                       @RequestParam(required = false) Integer year) {
        return filmService.mostPopularFilms(count, genreId, year);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Integer id) {
        return filmService.getFilmByIdOrThrow(id);
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getAllFilmsByDirector(@PathVariable Integer directorId,
                                                  @RequestParam(required = false) String sortBy) {
        return filmService.getAllFilmsByDirector(directorId, sortBy);
    }

    @PostMapping
    public Film addNewFilm(@Valid @RequestBody Film film) {
        return filmStorage.addNewFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        return filmStorage.updateFilm(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilmsWithFriend(@RequestParam("userId") Integer userId, @RequestParam("friendId") Integer friendId) {
        return filmService.getCommonFilmsWithFriend(userId, friendId);
    }

    @DeleteMapping("/{id}")
    public void deleteFilmById(@PathVariable Integer id) {
        filmService.deleteFilmById(id);
    }
}
