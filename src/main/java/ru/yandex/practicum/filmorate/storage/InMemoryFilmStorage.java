package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.interfaces.FilmStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @Override
    public Film addNewFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Новый фильм успешно добавлен: ID = {}, Название фильма = {}, Описание фильма = {}, " +
                        "Дата релиза = {}, Длительность = {}",
                film.getId(), film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration());
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Не указан Id фильма");
            throw new ValidationException("Id фильма должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            log.info("Фильм для обновления данных найден: Название фильма = {}, ID = {}", oldFilm.getName(), oldFilm.getId());
            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());
            log.info("Данные фильма успешно обновлены: ID = {}, Название фильма = {}, Описание фильма = {}, " +
                            "Дата релиза = {}, Длительность = {}",
                    oldFilm.getId(), oldFilm.getName(), oldFilm.getDescription(), oldFilm.getReleaseDate(), oldFilm.getDuration());
            return oldFilm;
        }
        log.warn("Ошибка при обновлении фильма: фильм с ID {} не найден", newFilm.getId());
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    public int getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public void saveFilm(Film film) {
        films.put(film.getId(), film);
    }

    public Film getFilmById(Integer filmId) {
        return films.get(filmId);
    }
}
