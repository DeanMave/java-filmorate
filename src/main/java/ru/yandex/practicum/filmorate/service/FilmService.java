package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final UserService userService;
    private final FilmDbStorage filmStorage;

    public FilmService(UserService userService, FilmDbStorage filmStorage) {
        this.userService = userService;
        this.filmStorage = filmStorage;
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = getFilmByIdOrThrow(filmId);
        userService.findUser(userId);
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, film.getName());
    }

    public void deleteLike(Integer filmId, Integer userId) {
        Film film = getFilmByIdOrThrow(filmId);
        userService.findUser(userId);
        filmStorage.deleteLike(filmId, userId);
        log.info("Пользователь {} убрал лайк у фильма {}", userId, film.getName());
    }

    public List<Film> mostPopularFilms(int size) {
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(size)
                .collect(Collectors.toList());
    }

    public Film getFilmByIdOrThrow(Integer filmId) {
        return filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));
    }
}
