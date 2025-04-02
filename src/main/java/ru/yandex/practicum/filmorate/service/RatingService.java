package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.RatingDbStorage;

import java.util.List;

@Service
public class RatingService {
    private final RatingDbStorage ratingStorage;

    public RatingService(RatingDbStorage ratingStorage) {
        this.ratingStorage = ratingStorage;
    }

    public List<Rating> getAllRatings() {
        return ratingStorage.gelAllRatings();
    }

    public Rating getRatingById(Integer id) {
        return ratingStorage.getRatingById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с ID " + id + " не найден"));
    }
}
