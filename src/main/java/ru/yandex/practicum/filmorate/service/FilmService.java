package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final Map<Integer, Film> films = new HashMap<>();
    private final UserService userService;

    public FilmService(UserService userService) {
        this.userService = userService;
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = films.get(filmId);
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
        Film film = films.get(filmId);
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
        return films.values().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(size)
                .collect(Collectors.toList());
    }

    public void addFilm(Film film) {
        films.put(film.getId(), film);
    }
}
