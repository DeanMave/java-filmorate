package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;
import ru.yandex.practicum.filmorate.storage.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final UserService userService;
    private final FilmDbStorage filmStorage;
    private final EventDbStorage eventDbStorage;

    public FilmService(UserService userService, FilmDbStorage filmStorage, EventDbStorage eventDbStorage) {
        this.userService = userService;
        this.filmStorage = filmStorage;
        this.eventDbStorage = eventDbStorage;
    }

    public List<Film> getAllFilmsByDirector(Integer directorId, String sortBy) {
        if ("likes".equalsIgnoreCase(sortBy)) {
            return filmStorage.getFilmsByDirectorSortedByLikes(directorId);
        } else if ("year".equalsIgnoreCase(sortBy)) {
            return filmStorage.getFilmsByDirectorSortedByYears(directorId);
        } else {
            throw new IllegalArgumentException("Некорректный параметр сортировки: " + sortBy);
        }
    }

    public List<Film> searchFilm(String query, String by) {
        if ("title".equalsIgnoreCase(by)) {
            return filmStorage.searchByTitle(query);
        } else if ("director".equalsIgnoreCase(by)) {
            return filmStorage.searchByDirector(query);
        } else if (("title,director".equalsIgnoreCase(by)) || "director,title".equalsIgnoreCase(by)) {
            return filmStorage.searchByTitleAndDirector(query);
        } else {
            throw new IllegalArgumentException("Некорректный параметр: " + by);
        }
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = getFilmByIdOrThrow(filmId);
        userService.findUser(userId);
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, film.getName());
        eventDbStorage.addEvent(EventType.LIKE, Operation.ADD, userId, filmId);
    }

    public void deleteLike(Integer filmId, Integer userId) {
        Film film = getFilmByIdOrThrow(filmId);
        userService.findUser(userId);
        filmStorage.deleteLike(filmId, userId);
        log.info("Пользователь {} убрал лайк у фильма {}", userId, film.getName());
        eventDbStorage.addEvent(EventType.LIKE, Operation.REMOVE, userId, filmId);
    }

    public void deleteFilmById(Integer filmId) {
        getFilmByIdOrThrow(filmId);
        filmStorage.deleteFilmById(filmId);
        log.info("Фильм {} удален", filmId);
    }

    public List<Film> mostPopularFilms(int size, Integer genreId, Integer year) {
        return filmStorage.getAllFilms().stream()
                .filter(film -> genreId == null || film.getGenres().stream().anyMatch(g -> g.getId().equals(genreId)))
                .filter(film -> year == null || film.getReleaseDate().getYear() == year)
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(size)
                .collect(Collectors.toList());
    }

    public Film getFilmByIdOrThrow(Integer filmId) {
        return filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));
    }

    public List<Film> getCommonFilmsWithFriend(Integer userId, Integer friendId) {
        userService.findUser(userId);
        userService.findUser(friendId);
        if (userService.findUser(userId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (userService.findUser(friendId) == null) {
            throw new NotFoundException("Друг пользователя не найден");
        }
        return filmStorage.getCommonFilmsWithFriend(userId, friendId);
    }

}
