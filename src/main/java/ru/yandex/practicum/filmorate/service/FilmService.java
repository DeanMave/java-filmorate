package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final UserService userService;
    private final InMemoryFilmStorage filmStorage;

    public FilmService(UserService userService, InMemoryFilmStorage filmStorage) {
        this.userService = userService;
        this.filmStorage = filmStorage;
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            throw new NotFoundException("Такого фильма не существует");
        }
        if (userService.findUser(userId) == null) {
            throw new NotFoundException("Такого пользователя не существует");
        }
        if (film != null && userId != null) {
            film.getLikes().add(userId);
            log.info("Лайк к фильму успешно добавлен: ID = {}, Название фильма = {}, Описание фильма = {}, " +
                            "Дата релиза = {}, Длительность = {}",
                    film.getId(), film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration());
        }
    }

    public void deleteLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            throw new NotFoundException("Такого фильма не существует");
        }
        if (userService.findUser(userId) == null) {
            throw new NotFoundException("Такого пользователя не существует");
        }
        if (film != null && userId != null) {
            film.getLikes().remove(userId);
            log.info("Лайк к фильму успешно удален: ID = {}, Название фильма = {}, Описание фильма = {}, " +
                            "Дата релиза = {}, Длительность = {}",
                    film.getId(), film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration());
        }
    }

    public List<Film> mostPopularFilms(int size) {
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(size)
                .collect(Collectors.toList());
    }

    public void addFilm(Film film) {
        filmStorage.saveFilm(film);
    }
}
