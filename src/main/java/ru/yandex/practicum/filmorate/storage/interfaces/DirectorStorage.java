package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    List<Director> getAllDirectors();

    Optional<Director> findById(Integer id);

    Director addNewDirector(Director director);

    Director updateDirector(Director newDirector);

    void deleteDirector(Integer id);
}
