package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorDbStorage;

@Service
public class DirectorService {
    private final DirectorDbStorage directorStorage;

    public DirectorService(DirectorDbStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Director getDirectorByIdOrThrow(Integer id) {
        return directorStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Режиссер с ID " + id + " не найден"));
    }
}
