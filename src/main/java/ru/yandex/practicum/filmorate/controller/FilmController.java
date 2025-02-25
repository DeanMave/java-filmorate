package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film addNewFilm(@RequestBody Film film) {
        if (film.getName() == null || film.getName().isEmpty()) {
            log.warn("Ошибка при добавлении фильма: название фильма пустое");
            throw new ValidationException("Название фильма не должно быть пустым");
        }
        if (film.getDescription().length() > 200) {
            log.warn("Ошибка при добавлении фильма: длина описания фильма превышает 200 символов");
            throw new ValidationException("Максимальная длина описания фильма — 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Ошибка при добавлении фильма: дата релиза фильма раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза фильма должна быть не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() < 0) {
            log.warn("Ошибка при добавлении фильма: отрицательная продолжительность фильма");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Новый фильм успешно добавлен: ID = {}, Название фильма = {}, Описание фильма = {}, " +
                        "Дата релиза = {}, Длительность = {}"
                , film.getId(), film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration());
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Не указан Id фильма");
            throw new ValidationException("Id фильма должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            log.info("Фильм для обновления данных найден: Название фильма = {}, ID = {}", oldFilm.getName(), oldFilm.getId());
            if (newFilm.getName() == null || newFilm.getName().isEmpty()) {
                log.warn("Ошибка при обновлении фильма: название фильма пустое");
                throw new ValidationException("Название фильма не должно быть пустым");
            }
            if (newFilm.getDescription().length() > 200) {
                log.warn("Ошибка при обновлении фильма: длина описания фильма превышает 200 символов");
                throw new ValidationException("Максимальная длина описания фильма — 200 символов");
            }
            if (newFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                log.warn("Ошибка при обновлении фильма: дата релиза фильма раньше 28 декабря 1895 года");
                throw new ValidationException("Дата релиза фильма должна быть не раньше 28 декабря 1895 года");
            }
            if (newFilm.getDuration() < 0) {
                log.warn("Ошибка при обновлении фильма: отрицательная продолжительность фильма");
                throw new ValidationException("Продолжительность фильма должна быть положительным числом");
            }
            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());
            log.info("Данные фильма успешно обновлены: ID = {}, Название фильма = {}, Описание фильма = {}, " +
                            "Дата релиза = {}, Длительность = {}"
                    , oldFilm.getId(), oldFilm.getName(), oldFilm.getDescription(), oldFilm.getReleaseDate(), oldFilm.getDuration());
            return oldFilm;
        }
        log.warn("Ошибка при обновлении фильма: фильм с ID {} не найден", newFilm.getId());
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    private int getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
