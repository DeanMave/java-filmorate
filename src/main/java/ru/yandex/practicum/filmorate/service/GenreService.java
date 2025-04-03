package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;

@Service
public class GenreService {
    private final GenreDbStorage genreStorage;

    public GenreService(GenreDbStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public List<Genre> getAllGenres() {
        return genreStorage.getAllGenres();
    }

    public Genre getGenreById(Integer id) {
        return genreStorage.getGenreById(id)
                        .orElseThrow(() -> new NotFoundException("Жанр с ID " + id + " не найден"));
    }
}
