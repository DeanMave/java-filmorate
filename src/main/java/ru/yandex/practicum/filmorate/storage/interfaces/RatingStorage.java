package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.Rating;

import java.util.List;
import java.util.Optional;

public interface RatingStorage {
    List<Rating> gelAllRatings();

    Optional<Rating> getRatingById(Integer id);
}
